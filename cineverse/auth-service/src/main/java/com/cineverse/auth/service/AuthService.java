package com.cineverse.auth.service;

import com.cineverse.auth.dto.*;
import com.cineverse.auth.entity.User;
import com.cineverse.auth.exception.AuthException;
import com.cineverse.auth.messaging.UserEventPublisher;
import com.cineverse.auth.repository.UserRepository;
import com.cineverse.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

/**
 * Core authentication business logic.
 *
 * Token blacklisting strategy:
 *  - On logout, the token's JTI (or the token itself) is stored in Redis
 *    with a TTL equal to the token's remaining lifetime.
 *  - The API Gateway checks this blacklist before forwarding requests.
 *  - This gives us stateless JWT benefits while still supporting instant revocation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String REFRESH_PREFIX = "jwt:refresh:";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate redisTemplate;
    private final UserEventPublisher eventPublisher;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Validate uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AuthException("Email already registered");
        }
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AuthException("Username already taken");
        }

        User user = User.builder()
            .username(request.getUsername())
            .email(request.getEmail())
            .passwordHash(passwordEncoder.encode(request.getPassword()))
            .displayName(request.getDisplayName())
            .roles(Set.of("ROLE_USER"))
            .build();

        user = userRepository.save(user);
        log.info("New user registered: {}", user.getId());

        // Publish event asynchronously — downstream services can react
        // (e.g., create default watchlist in movie-service)
        eventPublisher.publishUserRegistered(user);

        return buildAuthResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailOrUsername(request.getIdentifier())
            .orElseThrow(() -> new AuthException("Invalid credentials"));

        if (!user.isActive()) {
            throw new AuthException("Account is disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid credentials");
        }

        log.info("User logged in: {}", user.getId());
        return buildAuthResponse(user);
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new AuthException("Invalid refresh token");
        }

        var claims = jwtTokenProvider.validateAndParseClaims(refreshToken);
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new AuthException("Not a refresh token");
        }

        // Check it hasn't been revoked
        String storedRefresh = redisTemplate.opsForValue().get(REFRESH_PREFIX + claims.getSubject());
        if (!refreshToken.equals(storedRefresh)) {
            throw new AuthException("Refresh token revoked or expired");
        }

        User user = userRepository.findById(claims.getSubject())
            .orElseThrow(() -> new AuthException("User not found"));

        return buildAuthResponse(user);
    }

    public void logout(String accessToken, String userId) {
        // Blacklist the access token until it naturally expires
        var claims = jwtTokenProvider.validateAndParseClaims(accessToken);
        long remainingMs = claims.getExpiration().getTime() - System.currentTimeMillis();
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + accessToken,
                userId,
                Duration.ofMillis(remainingMs)
            );
        }
        // Remove refresh token
        redisTemplate.delete(REFRESH_PREFIX + userId);
        log.info("User logged out: {}", userId);
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }

    // ─── Private Helpers ─────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtTokenProvider.generateAccessToken(
            user.getId(), user.getUsername(), user.getRoles());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        // Store refresh token in Redis (TTL = 7 days)
        redisTemplate.opsForValue().set(
            REFRESH_PREFIX + user.getId(),
            refreshToken,
            Duration.ofMillis(604_800_000L)
        );

        return AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(jwtTokenProvider.getExpiryMs() / 1000)
            .user(AuthResponse.UserSummary.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .roles(user.getRoles())
                .build())
            .build();
    }
}
