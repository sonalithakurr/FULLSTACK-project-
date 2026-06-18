import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Star, BookmarkPlus, BookmarkCheck } from 'lucide-react';
import { moviesApi } from '../api/movies';
import useAuthStore from '../store/authStore';

/**
 * MovieCard — displays a movie in grid/list views.
 *
 * Props:
 *  - movie: MovieDto from the backend
 *  - inWatchlist: boolean — whether this movie is in the user's watchlist
 *  - onWatchlistChange: callback(movieId, added: boolean)
 */
function MovieCard({ movie, inWatchlist = false, onWatchlistChange }) {
    const { isAuthenticated } = useAuthStore();
    const [watchlisted, setWatchlisted] = useState(inWatchlist);
    const [loading, setLoading] = useState(false);

    const handleWatchlistToggle = async (e) => {
        e.preventDefault(); // Don't navigate to movie detail
        if (!isAuthenticated) {
            alert('Please log in to manage your watchlist');
            return;
        }
        setLoading(true);
        try {
            if (watchlisted) {
                await moviesApi.removeFromWatchlist(movie.id);
                setWatchlisted(false);
                onWatchlistChange?.(movie.id, false);
            } else {
                await moviesApi.addToWatchlist(movie.id);
                setWatchlisted(true);
                onWatchlistChange?.(movie.id, true);
            }
        } catch (err) {
            console.error('Watchlist toggle failed', err);
        } finally {
            setLoading(false);
        }
    };

    const posterSrc = movie.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster';

    return (
        <article className="movie-card" aria-label={`Movie: ${movie.title}`}>
            <Link to={`/movies/${movie.id}`} className="movie-card__link">
                <div className="movie-card__poster-wrapper">
                    <img
                        src={posterSrc}
                        alt={`Poster for ${movie.title}`}
                        className="movie-card__poster"
                        loading="lazy"
                    />
                    <div className="movie-card__overlay">
                        <span className="movie-card__genres">
                            {movie.genres?.slice(0, 2).join(' · ')}
                        </span>
                    </div>
                </div>

                <div className="movie-card__info">
                    <h3 className="movie-card__title">{movie.title}</h3>

                    <div className="movie-card__meta">
                        <span className="movie-card__year">
                            {movie.releaseDate
                                ? new Date(movie.releaseDate).getFullYear()
                                : 'TBA'}
                        </span>

                        {movie.averageRating && (
                            <span className="movie-card__rating" aria-label={`Rating: ${movie.averageRating} out of 10`}>
                                <Star size={14} aria-hidden="true" />
                                {Number(movie.averageRating).toFixed(1)}
                            </span>
                        )}

                        {movie.contentRating && (
                            <span className="movie-card__content-rating">
                                {movie.contentRating}
                            </span>
                        )}
                    </div>
                </div>
            </Link>

            {isAuthenticated && (
                <button
                    className={`movie-card__watchlist-btn ${watchlisted ? 'active' : ''}`}
                    onClick={handleWatchlistToggle}
                    disabled={loading}
                    aria-label={watchlisted ? 'Remove from watchlist' : 'Add to watchlist'}
                    aria-pressed={watchlisted}
                >
                    {watchlisted
                        ? <BookmarkCheck size={18} aria-hidden="true" />
                        : <BookmarkPlus size={18} aria-hidden="true" />}
                </button>
            )}
        </article>
    );
}

export default MovieCard;
