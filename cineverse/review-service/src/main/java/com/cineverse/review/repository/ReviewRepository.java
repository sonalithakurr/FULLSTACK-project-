package com.cineverse.review.repository;

import com.cineverse.review.document.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends MongoRepository<Review, String> {

    Page<Review> findByMovieIdOrderByCreatedAtDesc(String movieId, Pageable pageable);

    Optional<Review> findByMovieIdAndUserId(String movieId, String userId);

    boolean existsByMovieIdAndUserId(String movieId, String userId);

    long countByMovieId(String movieId);

    // Aggregation to compute average rating for a movie
    @Aggregation(pipeline = {
        "{ $match: { movieId: ?0 } }",
        "{ $group: { _id: null, avg: { $avg: '$rating' }, count: { $sum: 1 } } }"
    })
    RatingSummary getRatingSummaryByMovieId(String movieId);

    interface RatingSummary {
        Double getAvg();
        Long getCount();
    }
}
