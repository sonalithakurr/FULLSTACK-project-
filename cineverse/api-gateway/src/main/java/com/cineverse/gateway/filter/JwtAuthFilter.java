package com.cineverse.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;

/**
 * JWT Authentication Gateway Filter
 *
 * This filter:
 *  1. Extracts Bearer token from Authorization header
 *  2. Validates signature and expiry
 *  3. Checks the Redis blacklist (for logged-out tokens)
 *  4. Injects X-User-Id, X-Username, X-Roles headers for downstream services
 *
 * Downstream services trust these headers completely — they never parse JWTs.
 * This is a key architectural pattern: auth concern is centralized at the gateway.
 */
@Slf4j
@Component
public class JwtAuthFilter extends AbstractGatewayFilterFactory<JwtAuthFilter.Config> {

    private static final String BLACKLIST_PREFIX = "jwt:blacklist:";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final ReactiveStringRedisTemplate redisTemplate;

    public JwtAuthFilter(
            @Value("${jwt.secret}") String secret,
            ReactiveStringRedisTemplate redisTemplate) {
        super(Config.class);
        byte[] keyBytes = Decoders.BASE64.decode(
            java.util.Base64.getEncoder().encodeToString(secret.getBytes()));
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
        this.redisTemplate = redisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return unauthorized(exchange, "Missing Authorization header");
            }

            String token = authHeader.substring(BEARER_PREFIX.length());

            // Parse and validate JWT
            Claims claims;
            try {
                claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            } catch (JwtException e) {
                log.warn("Invalid JWT: {}", e.getMessage());
                return unauthorized(exchange, "Invalid token");
            }

            // Check blacklist in Redis (reactive)
            return redisTemplate.hasKey(BLACKLIST_PREFIX + token)
                .flatMap(blacklisted -> {
                    if (Boolean.TRUE.equals(blacklisted)) {
                        return unauthorized(exchange, "Token has been revoked");
                    }

                    // Inject user context headers for downstream services
                    ServerWebExchange mutated = exchange.mutate()
                        .request(r -> r
                            .header("X-User-Id", claims.getSubject())
                            .header("X-Username", String.valueOf(claims.get("username")))
                            .header("X-Roles", String.valueOf(claims.get("roles")))
                            .header("X-Display-Name", String.valueOf(
                                claims.getOrDefault("displayName", ""))))
                        .build();

                    return chain.filter(mutated);
                });
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String reason) {
        log.warn("Request rejected: {}", reason);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    public static class Config {
        // Configuration properties if needed in the future
    }
}
