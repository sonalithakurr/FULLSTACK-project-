package com.cineverse.review.controller;

import com.cineverse.review.document.Review;
import com.cineverse.review.dto.CreateReviewRequest;
import com.cineverse.review.repository.ReviewRepository;
import com.cineverse.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Review REST Controller
 * Base path: /api/v1/reviews
 *
 * User identity comes from X-User-Id, X-Username, X-Display-Name headers
 * injected by the API Gateway after JWT validation.
 */
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** GET /api/v1/reviews/movie/{movieId}?page=0&size=10 */
    @GetMapping("/movie/{movieId}")
    public ResponseEntity<Page<Review>> getReviews(
            @PathVariable String movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(reviewService.getReviewsForMovie(movieId, page, size));
    }

    /** GET /api/v1/reviews/movie/{movieId}/summary */
    @GetMapping("/movie/{movieId}/summary")
    public ResponseEntity<ReviewRepository.RatingSummary> getSummary(
            @PathVariable String movieId) {
        return ResponseEntity.ok(reviewService.getRatingSummary(movieId));
    }

    /** POST /api/v1/reviews */
    @PostMapping
    public ResponseEntity<Review> createReview(
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Username") String username,
            @RequestHeader(value = "X-Display-Name", defaultValue = "") String displayName) {
        Review review = reviewService.createReview(request, userId, username, displayName);
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    /** PUT /api/v1/reviews/{id} */
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(
            @PathVariable String id,
            @Valid @RequestBody CreateReviewRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(reviewService.updateReview(id, request, userId));
    }

    /** POST /api/v1/reviews/{id}/helpful */
    @PostMapping("/{id}/helpful")
    public ResponseEntity<Review> markHelpful(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(reviewService.markHelpful(id, userId));
    }
}
