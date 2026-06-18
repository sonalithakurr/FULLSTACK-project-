package com.cineverse.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "watchlist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "movieId"}),
    indexes = @Index(name = "idx_watchlist_user", columnList = "userId"))
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;    // Reference to auth-service User (cross-service — not a FK)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movieId", nullable = false)
    private Movie movie;

    @CreatedDate
    @Column(updatable = false)
    private Instant addedAt;
}
