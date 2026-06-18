package com.cineverse.movie.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.Map;

/**
 * Fine-grained cache TTL configuration per cache name.
 * Different data has different staleness tolerance:
 *  - Movie details rarely change → long TTL
 *  - Search results should be fresh → short TTL
 */
@Configuration
public class CacheConfig {

    @Value("${cineverse.cache.movies-ttl:600}") private long moviesTtl;
    @Value("${cineverse.cache.movie-detail-ttl:3600}") private long movieDetailTtl;
    @Value("${cineverse.cache.search-ttl:120}") private long searchTtl;
    @Value("${cineverse.cache.genres-ttl:86400}") private long genresTtl;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory factory) {
        var jsonSerializer = RedisSerializationContext.SerializationPair
            .fromSerializer(new GenericJackson2JsonRedisSerializer());

        RedisCacheConfiguration defaults = RedisCacheConfiguration.defaultCacheConfig()
            .serializeValuesWith(jsonSerializer)
            .disableCachingNullValues();

        return RedisCacheManager.builder(factory)
            .cacheDefaults(defaults)
            .withInitialCacheConfigurations(Map.of(
                "movies",          defaults.entryTtl(Duration.ofSeconds(moviesTtl)),
                "movie-detail",    defaults.entryTtl(Duration.ofSeconds(movieDetailTtl)),
                "movie-search",    defaults.entryTtl(Duration.ofSeconds(searchTtl)),
                "movies-by-genre", defaults.entryTtl(Duration.ofSeconds(moviesTtl)),
                "top-rated",       defaults.entryTtl(Duration.ofSeconds(moviesTtl)),
                "genres",          defaults.entryTtl(Duration.ofSeconds(genresTtl))
            ))
            .build();
    }
}
