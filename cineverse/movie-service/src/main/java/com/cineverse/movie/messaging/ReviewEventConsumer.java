package com.cineverse.movie.messaging;

import com.cineverse.movie.service.MovieService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Consumes review events from the review-service.
 * When a new review is submitted, the movie's cached average rating is invalidated.
 *
 * Why async messaging instead of direct HTTP call?
 *  - Decoupling: review-service doesn't depend on movie-service being available
 *  - Resilience: events are durable — if movie-service is down, events queue up
 *  - Performance: review submission doesn't block waiting for cache invalidation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventConsumer {

    private final MovieService movieService;

    @RabbitListener(
        queues = "review.rating.updated.queue",
        ackMode = "MANUAL"
    )
    public void onRatingUpdated(Map<String, Object> event,
                                Channel channel,
                                @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            String movieId = (String) event.get("movieId");
            // Evict stale cache so next request fetches fresh data
            movieService.evictMovieCache(movieId);

            // Optionally update the denormalized rating in the movies table
            // to avoid cross-service queries on the hot read path
            // movieService.updateCachedRating(movieId, newAvg, newCount);

            channel.basicAck(tag, false);
            log.info("Processed rating update for movieId={}", movieId);
        } catch (Exception e) {
            log.error("Failed to process rating update event", e);
            channel.basicNack(tag, false, false); // Send to DLX
        }
    }
}
