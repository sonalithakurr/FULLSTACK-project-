package com.cineverse.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * Rate limiting key resolvers.
 *
 * Two strategies:
 *  - IP-based: for public endpoints (per client IP)
 *  - User-based: for authenticated endpoints (per user ID from JWT)
 *    This prevents one user from consuming others' quotas.
 */
@Configuration
public class RateLimitConfig {

    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.justOrEmpty(
            exchange.getRequest().getRemoteAddress())
            .map(addr -> addr.getAddress().getHostAddress())
            .defaultIfEmpty("unknown");
    }

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.justOrEmpty(
            exchange.getRequest().getHeaders().getFirst("X-User-Id"))
            .defaultIfEmpty("anonymous");
    }
}
