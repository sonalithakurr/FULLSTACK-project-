package com.cineverse.movie.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies", indexes = {
    @Index(name = "idx_movies_title", columnList = "title"),
    @Index(name = "idx_movies_release_date", columnList = "releaseDate"),
    @Index(name = "idx_movies_avg_rating", columnList = "averageRating")
})
@EntityListeners(AuditingEntityListener.class)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private LocalDate releaseDate;

    @Column(length = 10)
    private String contentRating;   // G, PG, PG-13, R, NC-17

    private Integer runtimeMinutes;

    @Column(precision = 3, scale = 1)
    private BigDecimal averageRating;   // Cached from review-service via events

    private Integer reviewCount;

    private String posterUrl;
    private String backdropUrl;
    private String trailerUrl;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_genres",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    @Builder.Default
    private Set<Genre> genres = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "movie_cast",
        joinColumns = @JoinColumn(name = "movie_id"),
        inverseJoinColumns = @JoinColumn(name = "person_id")
    )
    @Builder.Default
    private Set<Person> cast = new HashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
