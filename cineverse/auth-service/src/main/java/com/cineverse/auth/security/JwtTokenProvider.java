package com.cineverse.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Set;

/**
 * JWT Token Provider
 *
 * Why HMAC-SHA256?
 *  - Symmetric signing is sufficient for our microservices architecture
 *    where the gateway and auth service share the same secret.
 *  - RSA would be needed if third-party services needed to verify tokens
 *    without access to the secret key.
 *
 * Token structure:
 *  - sub: userId
 *  - username: username
 *  - roles: comma-separated roles
 *  - iat: issued at
 *  - exp: expiry
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long expiryMs;
    private final long refreshExpiryMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiry-ms}") long expiryMs,
            @Value("${jwt.refresh-expiry-ms:604800000}") long refreshExpiryMs) {

        // Derive a 256-bit key from the configured secret
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secret.getBytes()));
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiryMs = expiryMs;
        this.refreshExpiryMs = refreshExpiryMs;
    }

    public String generateAccessToken(String userId, String username, Set<String> roles) {
        return Jwts.builder()
            .subject(userId)
            .claim("username", username)
            .claim("roles", String.join(",", roles))
            .claim("type", "ACCESS")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiryMs))
            .signWith(signingKey)
            .compact();
    }

    public String generateRefreshToken(String userId) {
        return Jwts.builder()
            .subject(userId)
            .claim("type", "REFRESH")
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + refreshExpiryMs))
            .signWith(signingKey)
            .compact();
    }

    public Claims validateAndParseClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public boolean isValid(String token) {
        try {
            validateAndParseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public String getUserIdFromToken(String token) {
        return validateAndParseClaims(token).getSubject();
    }

    public long getExpiryMs() {
        return expiryMs;
    }
}
