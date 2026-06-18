package com.cineverse.review.service;

import com.cineverse.review.document.Review;
import com.cineverse.review.dto.CreateReviewRequest;
import com.cineverse.review.exception.ReviewException;
import com.cineverse.review.messaging.ReviewEventPublisher;
import com.cineverse.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewEventPublisher eventPublisher;

    @Cacheable(value = "movie-reviews", key = "#movieId + '-' + #page")
    public Page<Review> getReviewsForMovie(String movieId, int page, int size) {
        return reviewRepository.findByMovieIdOrderByCreatedAtDesc(
            movieId, PageRequest.of(page, size));
    }

    @CacheEvict(value = "movie-reviews", allEntries = true)
    public Review createReview(CreateReviewRequest request, String userId,
                               String username, String displayName) {
        if (reviewRepository.existsByMovieIdAndUserId(request.getMovieId(), userId)) {
            throw new ReviewException("You have already reviewed this movie");
        }

        Review review = Review.builder()
            .movieId(request.getMovieId())
            .userId(userId)
            .username(username)
            .userDisplayName(displayName)
            .rating(request.getRating())
            .title(request.getTitle())
            .content(request.getContent())
            .tags(request.getTags() != null ? request.getTags() : java.util.List.of())
            .containsSpoilers(request.isContainsSpoilers())
            .build();

        review = reviewRepository.save(review);

        // Notify movie-service to update its cached average rating
        var summary = reviewRepository.getRatingSummaryByMovieId(request.getMovieId());
        eventPublisher.publishRatingUpdated(
            request.getMovieId(),
            summary != null ? summary.getAvg() : request.getRating(),
            summary != null ? summary.getCount() : 1L
        );

        return review;
    }

    @CacheEvict(value = "movie-reviews", allEntries = true)
    public Review updateReview(String reviewId, CreateReviewRequest request, String userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));

        if (!review.getUserId().equals(userId)) {
            throw new ReviewException("You can only edit your own reviews");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setContainsSpoilers(request.isContainsSpoilers());

        return reviewRepository.save(review);
    }

    public Review markHelpful(String reviewId, String userId) {
        Review review = reviewRepository.findById(reviewId)
            .orElseThrow(() -> new ReviewException("Review not found"));

        if (!review.getHelpfulVoterIds().contains(userId)) {
            review.getHelpfulVoterIds().add(userId);
            review.setHelpfulCount(review.getHelpfulCount() + 1);
            review = reviewRepository.save(review);
        }
        return review;
    }

    public ReviewRepository.RatingSummary getRatingSummary(String movieId) {
        return reviewRepository.getRatingSummaryByMovieId(movieId);
    }
}
