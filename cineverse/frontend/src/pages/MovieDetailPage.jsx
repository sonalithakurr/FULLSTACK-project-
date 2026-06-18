import React, { useState } from 'react';
import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Star, Clock, Calendar } from 'lucide-react';
import { moviesApi } from '../api/movies';
import { reviewsApi } from '../api/reviews';
import ReviewForm from '../components/ReviewForm';
import useAuthStore from '../store/authStore';

function MovieDetailPage() {
    const { id } = useParams();
    const { isAuthenticated } = useAuthStore();
    const [showReviewForm, setShowReviewForm] = useState(false);

    const { data: movie, isLoading } = useQuery({
        queryKey: ['movie', id],
        queryFn: () => moviesApi.getMovie(id).then(r => r.data),
    });

    const { data: reviewsPage, refetch: refetchReviews } = useQuery({
        queryKey: ['reviews', id],
        queryFn: () => reviewsApi.getReviews(id, 0, 10).then(r => r.data),
        enabled: !!id,
    });

    if (isLoading) return <div className="loading-screen" aria-busy="true">Loading...</div>;
    if (!movie) return <div className="error-screen">Movie not found.</div>;

    return (
        <main className="movie-detail" aria-labelledby="movie-title">
            {/* Backdrop */}
            {movie.backdropUrl && (
                <div
                    className="movie-detail__backdrop"
                    style={{ backgroundImage: `url(${movie.backdropUrl})` }}
                    role="img"
                    aria-label={`Backdrop for ${movie.title}`}
                />
            )}

            <div className="movie-detail__content">
                {/* Poster + Meta */}
                <aside className="movie-detail__sidebar" aria-label="Movie details">
                    <img
                        src={movie.posterUrl || 'https://via.placeholder.com/300x450?text=No+Poster'}
                        alt={`Poster for ${movie.title}`}
                        className="movie-detail__poster"
                    />
                </aside>

                <article className="movie-detail__main">
                    <h1 id="movie-title" className="movie-detail__title">{movie.title}</h1>

                    <div className="movie-detail__badges" aria-label="Movie metadata">
                        {movie.contentRating && (
                            <span className="badge badge--rating">{movie.contentRating}</span>
                        )}
                        {movie.genres?.map(g => (
                            <span key={g} className="badge">{g}</span>
                        ))}
                    </div>

                    <div className="movie-detail__stats">
                        {movie.averageRating && (
                            <div className="stat" aria-label={`Rating: ${movie.averageRating} out of 10`}>
                                <Star size={20} aria-hidden="true" />
                                <strong>{Number(movie.averageRating).toFixed(1)}</strong>
                                <span>/ 10</span>
                                <small>({movie.reviewCount} reviews)</small>
                            </div>
                        )}
                        {movie.runtimeMinutes && (
                            <div className="stat" aria-label={`Runtime: ${movie.runtimeMinutes} minutes`}>
                                <Clock size={20} aria-hidden="true" />
                                <span>{movie.runtimeMinutes} min</span>
                            </div>
                        )}
                        {movie.releaseDate && (
                            <div className="stat" aria-label={`Released: ${movie.releaseDate}`}>
                                <Calendar size={20} aria-hidden="true" />
                                <span>{new Date(movie.releaseDate).getFullYear()}</span>
                            </div>
                        )}
                    </div>

                    {movie.description && (
                        <p className="movie-detail__description">{movie.description}</p>
                    )}

                    {/* Cast */}
                    {movie.cast?.length > 0 && (
                        <section aria-labelledby="cast-heading">
                            <h2 id="cast-heading" className="movie-detail__section-title">Cast</h2>
                            <ul className="cast-list" role="list">
                                {movie.cast.slice(0, 8).map(person => (
                                    <li key={person.id} className="cast-item">
                                        <span className="cast-item__name">{person.name}</span>
                                    </li>
                                ))}
                            </ul>
                        </section>
                    )}

                    {/* Review CTA */}
                    <div className="movie-detail__review-cta">
                        {isAuthenticated ? (
                            <button
                                className="btn btn--primary"
                                onClick={() => setShowReviewForm(!showReviewForm)}
                                aria-expanded={showReviewForm}
                            >
                                {showReviewForm ? 'Cancel' : 'Write a Review'}
                            </button>
                        ) : (
                            <p>
                                <a href="/login">Sign in</a> to write a review
                            </p>
                        )}
                    </div>

                    {showReviewForm && (
                        <ReviewForm
                            movieId={id}
                            onSuccess={() => {
                                setShowReviewForm(false);
                                refetchReviews();
                            }}
                        />
                    )}
                </article>
            </div>

            {/* Reviews */}
            <section className="reviews-section" aria-labelledby="reviews-heading">
                <h2 id="reviews-heading" className="reviews-section__title">
                    Reviews {reviewsPage?.totalElements ? `(${reviewsPage.totalElements})` : ''}
                </h2>
                {reviewsPage?.content?.length === 0 && (
                    <p className="reviews-section__empty">
                        No reviews yet. Be the first to review!
                    </p>
                )}
                {reviewsPage?.content?.map(review => (
                    <article key={review.id} className="review-card" aria-label={`Review by ${review.userDisplayName}`}>
                        <header className="review-card__header">
                            <span className="review-card__author">{review.userDisplayName}</span>
                            <span className="review-card__rating" aria-label={`${review.rating} out of 10`}>
                                <Star size={14} aria-hidden="true" />
                                {review.rating}/10
                            </span>
                            {review.containsSpoilers && (
                                <span className="badge badge--spoiler">Contains spoilers</span>
                            )}
                        </header>
                        <h3 className="review-card__title">{review.title}</h3>
                        <p className="review-card__content">{review.content}</p>
                    </article>
                ))}
            </section>
        </main>
    );
}

export default MovieDetailPage;
