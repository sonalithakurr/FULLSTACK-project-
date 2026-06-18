package com.cineverse.movie.controller;

import com.cineverse.movie.dto.MovieDetailDto;
import com.cineverse.movie.dto.MovieDto;
import com.cineverse.movie.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Movie REST Controller
 * Base path: /api/v1/movies
 *
 * The userId is extracted from the X-User-Id header injected by the API Gateway
 * after JWT validation — no need to parse JWT in this service.
 */
@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    /** GET /api/v1/movies?page=0&size=20&sortBy=releaseDate */
    @GetMapping
    public ResponseEntity<Page<MovieDto>> getMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "releaseDate") String sortBy) {
        return ResponseEntity.ok(movieService.getMovies(page, size, sortBy));
    }

    /** GET /api/v1/movies/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailDto> getMovie(@PathVariable String id) {
        return ResponseEntity.ok(movieService.getMovieById(id));
    }

    /** GET /api/v1/movies/search?q=inception&page=0&size=10 */
    @GetMapping("/search")
    public ResponseEntity<Page<MovieDto>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(movieService.searchMovies(q, page, size));
    }

    /** GET /api/v1/movies/genre/{slug}?page=0&size=20 */
    @GetMapping("/genre/{slug}")
    public ResponseEntity<Page<MovieDto>> byGenre(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.getMoviesByGenre(slug, page, size));
    }

    /** GET /api/v1/movies/top-rated */
    @GetMapping("/top-rated")
    public ResponseEntity<Page<MovieDto>> topRated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(movieService.getTopRated(page, size));
    }

    // ─── Watchlist Endpoints ─────────────────────────────────────────────────

    /** POST /api/v1/movies/{id}/watchlist */
    @PostMapping("/{id}/watchlist")
    public ResponseEntity<Void> addToWatchlist(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        movieService.addToWatchlist(userId, id);
        return ResponseEntity.ok().build();
    }

    /** DELETE /api/v1/movies/{id}/watchlist */
    @DeleteMapping("/{id}/watchlist")
    public ResponseEntity<Void> removeFromWatchlist(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        movieService.removeFromWatchlist(userId, id);
        return ResponseEntity.noContent().build();
    }

    /** GET /api/v1/movies/watchlist */
    @GetMapping("/watchlist")
    public ResponseEntity<List<MovieDto>> getWatchlist(
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(movieService.getWatchlist(userId));
    }
}
