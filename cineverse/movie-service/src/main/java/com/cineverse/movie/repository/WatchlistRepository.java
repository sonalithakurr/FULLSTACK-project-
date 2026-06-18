package com.cineverse.movie.repository;

import com.cineverse.movie.entity.Watchlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, String> {
    List<Watchlist> findByUserId(String userId);
    boolean existsByUserIdAndMovieId(String userId, String movieId);
    void deleteByUserIdAndMovieId(String userId, String movieId);
}
