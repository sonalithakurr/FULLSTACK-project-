package com.cineverse.auth.messaging;

import com.cineverse.auth.config.RabbitMQConfig;
import com.cineverse.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Publishes a user.registered event asynchronously.
     * The movie-service subscribes to this to create a default watchlist.
     */
    @Async
    public void publishUserRegistered(User user) {
        try {
            Map<String, Object> event = Map.of(
                "eventType", "USER_REGISTERED",
                "userId", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "occurredAt", Instant.now().toString()
            );
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                RabbitMQConfig.USER_REGISTERED_KEY,
                event
            );
            log.debug("Published USER_REGISTERED event for userId={}", user.getId());
        } catch (Exception e) {
            // Non-critical — log and continue. The movie-service can create
            // the watchlist lazily on first access.
            log.error("Failed to publish USER_REGISTERED event for userId={}", user.getId(), e);
        }
    }
}
