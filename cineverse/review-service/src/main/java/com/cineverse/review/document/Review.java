package com.cineverse.review.document;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Review MongoDB Document
 *
 * Why MongoDB for reviews?
 *  - Flexible schema: reviews can have varying metadata (tags, spoiler flags, etc.)
 *  - Embedded helpfulness votes keep reads fast (no join needed)
 *  - Easy to add fields like "reviewer_context" without migrations
 *  - High write throughput for review submissions
 */
@Document(collection = "reviews")
@CompoundIndex(def = "{'movieId': 1, 'createdAt': -1}")  // Main query pattern
@CompoundIndex(def = "{'userId': 1, 'movieId': 1}", unique = true)  // One review per user per movie
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Review {

    @Id
    private String id;

    @Indexed
    private String movieId;     // Reference to PostgreSQL movie ID

    @Indexed
    private String userId;      // Reference to PostgreSQL user ID

    private String username;    // Denormalized for fast display (avoids cross-service call)
    private String userDisplayName;

    private int rating;         // 1–10

    private String title;

    private String content;

    @Builder.Default
    private List<String> tags = new ArrayList<>();  // e.g., ["great-acting", "slow-pacing"]

    private boolean containsSpoilers;

    @Builder.Default
    private int helpfulCount = 0;

    @Builder.Default
    private List<String> helpfulVoterIds = new ArrayList<>(); // Users who found this helpful

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
