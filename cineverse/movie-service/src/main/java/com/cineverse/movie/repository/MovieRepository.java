package com.cineverse.movie.repository;

import com.cineverse.movie.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository extends JpaRepository<Movie, String>,
                                         JpaSpecificationExecutor<Movie> {

    Page<Movie> findByActiveTrueOrderByReleaseDateDesc(Pageable pageable);

    // Full-text search across title and description
    @Query("""
        SELECT m FROM Movie m
        WHERE m.active = true
        AND (LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
             OR LOWER(m.description) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Movie> searchMovies(@Param("query") String query, Pageable pageable);

    @Query("""
        SELECT m FROM Movie m
        JOIN m.genres g
        WHERE m.active = true AND g.slug = :genreSlug
        """)
    Page<Movie> findByGenreSlug(@Param("genreSlug") String genreSlug, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.active = true ORDER BY m.averageRating DESC")
    Page<Movie> findTopRated(Pageable pageable);
}
