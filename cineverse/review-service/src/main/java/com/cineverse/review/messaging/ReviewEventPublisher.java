package com.cineverse.review.messaging;

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
public class ReviewEventPublisher {

    private static final String REVIEW_EXCHANGE = "cineverse.reviews";
    private static final String RATING_UPDATED_KEY = "review.rating.updated";

    private final RabbitTemplate rabbitTemplate;

    @Async
    public void publishRatingUpdated(String movieId, double newAvgRating, long reviewCount) {
        try {
            Map<String, Object> event = Map.of(
                "eventType", "RATING_UPDATED",
                "movieId", movieId,
                "averageRating", newAvgRating,
                "reviewCount", reviewCount,
                "occurredAt", Instant.now().toString()
            );
            rabbitTemplate.convertAndSend(REVIEW_EXCHANGE, RATING_UPDATED_KEY, event);
            log.debug("Published RATING_UPDATED event for movieId={}", movieId);
        } catch (Exception e) {
            log.error("Failed to publish RATING_UPDATED event for movieId={}", movieId, e);
        }
    }
}
