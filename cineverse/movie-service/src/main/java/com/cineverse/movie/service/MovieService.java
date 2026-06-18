package com.cineverse.movie.service;

import com.cineverse.movie.dto.MovieDetailDto;
import com.cineverse.movie.dto.MovieDto;
import com.cineverse.movie.entity.Movie;
import com.cineverse.movie.entity.Watchlist;
import com.cineverse.movie.exception.ResourceNotFoundException;
import com.cineverse.movie.repository.MovieRepository;
import com.cineverse.movie.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Movie business logic with Redis caching.
 *
 * Caching strategy:
 *  - Movie lists: cached 10 min (changes on new releases)
 *  - Movie details: cached 1 hour (rarely changes)
 *  - Search results: cached 2 min (balance freshness vs performance)
 *  - Genre list: cached 24 hours (effectively static)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final WatchlistRepository watchlistRepository;

    @Cacheable(value = "movies", key = "#page + '-' + #size + '-' + #sortBy")
    @Transactional(readOnly = true)
    public Page<MovieDto> getMovies(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return movieRepository.findByActiveTrueOrderByReleaseDateDesc(pageable)
            .map(this::toDto);
    }

    @Cacheable(value = "movie-detail", key = "#id")
    @Transactional(readOnly = true)
    public MovieDetailDto getMovieById(String id) {
        Movie movie = movieRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + id));
        return toDetailDto(movie);
    }

    @Cacheable(value = "movie-search", key = "#query + '-' + #page + '-' + #size")
    @Transactional(readOnly = true)
    public Page<MovieDto> searchMovies(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.searchMovies(query, pageable).map(this::toDto);
    }

    @Cacheable(value = "movies-by-genre", key = "#genreSlug + '-' + #page")
    @Transactional(readOnly = true)
    public Page<MovieDto> getMoviesByGenre(String genreSlug, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findByGenreSlug(genreSlug, pageable).map(this::toDto);
    }

    @Cacheable(value = "top-rated", key = "#page")
    @Transactional(readOnly = true)
    public Page<MovieDto> getTopRated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return movieRepository.findTopRated(pageable).map(this::toDto);
    }

    // ─── Watchlist ────────────────────────────────────────────────────────────

    @Transactional
    public void addToWatchlist(String userId, String movieId) {
        if (watchlistRepository.existsByUserIdAndMovieId(userId, movieId)) {
            return; // Idempotent
        }
        Movie movie = movieRepository.findById(movieId)
            .orElseThrow(() -> new ResourceNotFoundException("Movie not found: " + movieId));
        watchlistRepository.save(Watchlist.builder()
            .userId(userId)
            .movie(movie)
            .build());
    }

    @Transactional
    public void removeFromWatchlist(String userId, String movieId) {
        watchlistRepository.deleteByUserIdAndMovieId(userId, movieId);
    }

    @Transactional(readOnly = true)
    public List<MovieDto> getWatchlist(String userId) {
        return watchlistRepository.findByUserId(userId).stream()
            .map(w -> toDto(w.getMovie()))
            .toList();
    }

    // ─── Cache Eviction (called by RabbitMQ consumer when ratings update) ────

    @CacheEvict(value = {"movie-detail", "top-rated"}, key = "#movieId")
    public void evictMovieCache(String movieId) {
        log.debug("Evicted cache for movieId={}", movieId);
    }

    // ─── Mappers ─────────────────────────────────────────────────────────────

    private MovieDto toDto(Movie m) {
        MovieDto dto = new MovieDto();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setDescription(m.getDescription());
        dto.setReleaseDate(m.getReleaseDate());
        dto.setContentRating(m.getContentRating());
        dto.setRuntimeMinutes(m.getRuntimeMinutes());
        dto.setAverageRating(m.getAverageRating());
        dto.setReviewCount(m.getReviewCount());
        dto.setPosterUrl(m.getPosterUrl());
        dto.setGenres(m.getGenres().stream().map(g -> g.getName()).toList());
        return dto;
    }

    private MovieDetailDto toDetailDto(Movie m) {
        MovieDetailDto dto = new MovieDetailDto();
        dto.setId(m.getId());
        dto.setTitle(m.getTitle());
        dto.setDescription(m.getDescription());
        dto.setReleaseDate(m.getReleaseDate());
        dto.setContentRating(m.getContentRating());
        dto.setRuntimeMinutes(m.getRuntimeMinutes());
        dto.setAverageRating(m.getAverageRating());
        dto.setReviewCount(m.getReviewCount());
        dto.setPosterUrl(m.getPosterUrl());
        dto.setBackdropUrl(m.getBackdropUrl());
        dto.setTrailerUrl(m.getTrailerUrl());
        dto.setGenres(m.getGenres().stream().map(g -> g.getName()).toList());
        dto.setCast(m.getCast().stream()
            .filter(p -> p.getRole() == com.cineverse.movie.entity.Person.PersonRole.ACTOR)
            .map(p -> {
                var person = new MovieDetailDto.PersonDto();
                person.setId(p.getId());
                person.setName(p.getName());
                person.setRole(p.getRole().name());
                person.setPhotoUrl(p.getPhotoUrl());
                return person;
            }).toList());
        dto.setDirectors(m.getCast().stream()
            .filter(p -> p.getRole() == com.cineverse.movie.entity.Person.PersonRole.DIRECTOR)
            .map(p -> {
                var person = new MovieDetailDto.PersonDto();
                person.setId(p.getId());
                person.setName(p.getName());
                person.setRole(p.getRole().name());
                return person;
            }).toList());
        return dto;
    }
}
