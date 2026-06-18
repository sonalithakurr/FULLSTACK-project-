package com.cineverse.auth.controller;

import com.cineverse.auth.dto.*;
import com.cineverse.auth.security.JwtTokenProvider;
import com.cineverse.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Authentication REST Controller
 *
 * Base path: /api/v1/auth
 * All endpoints are PUBLIC (no JWT required) except /logout and /validate
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * POST /api/v1/auth/register
     * Register a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/login
     * Authenticate with email/username + password. Returns JWT tokens.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/v1/auth/refresh
     * Exchange a valid refresh token for a new access token.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> body) {
        String refreshToken = body.get("refreshToken");
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }

    /**
     * POST /api/v1/auth/logout
     * Blacklist the current access token (requires valid JWT in header).
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = extractToken(authHeader);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        authService.logout(token, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/v1/auth/validate
     * Used by the API Gateway to validate tokens.
     * Returns 200 with claims if valid, 401 if not.
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader("Authorization") String authHeader) {

        String token = extractToken(authHeader);

        if (!jwtTokenProvider.isValid(token) || authService.isTokenBlacklisted(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        var claims = jwtTokenProvider.validateAndParseClaims(token);
        return ResponseEntity.ok(Map.of(
            "userId", claims.getSubject(),
            "username", claims.get("username"),
            "roles", claims.get("roles"),
            "valid", true
        ));
    }

    // ─── Private Helpers ────────────────────────────────────────────────────

    private String extractToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }
        return authHeader.substring(7);
    }
}
