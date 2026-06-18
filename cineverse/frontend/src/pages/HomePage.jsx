import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { Link } from 'react-router-dom';
import MovieCard from '../components/MovieCard';
import { moviesApi } from '../api/movies';

function HomePage() {
    const { data: newReleases, isLoading: loadingNew } = useQuery({
        queryKey: ['movies', 'new'],
        queryFn: () => moviesApi.getMovies(0, 8).then(r => r.data),
    });

    const { data: topRated, isLoading: loadingTop } = useQuery({
        queryKey: ['movies', 'top-rated'],
        queryFn: () => moviesApi.getTopRated(0, 8).then(r => r.data),
    });

    return (
        <main className="home-page">
            {/* Hero */}
            <section className="hero" aria-label="Welcome banner">
                <div className="hero__content">
                    <h1 className="hero__title">Discover Your Next Favorite Film</h1>
                    <p className="hero__subtitle">
                        Browse thousands of movies, read reviews, and build your watchlist.
                    </p>
                    <Link to="/movies" className="btn btn--primary btn--large">
                        Browse Movies
                    </Link>
                </div>
            </section>

            {/* New Releases */}
            <section className="movie-section" aria-labelledby="new-releases-heading">
                <div className="movie-section__header">
                    <h2 id="new-releases-heading">New Releases</h2>
                    <Link to="/movies" className="movie-section__see-all">
                        See all →
                    </Link>
                </div>
                {loadingNew ? (
                    <MovieGridSkeleton count={8} />
                ) : (
                    <div className="movie-grid">
                        {newReleases?.content?.map(movie => (
                            <MovieCard key={movie.id} movie={movie} />
                        ))}
                    </div>
                )}
            </section>

            {/* Top Rated */}
            <section className="movie-section" aria-labelledby="top-rated-heading">
                <div className="movie-section__header">
                    <h2 id="top-rated-heading">Top Rated</h2>
                    <Link to="/movies/top-rated" className="movie-section__see-all">
                        See all →
                    </Link>
                </div>
                {loadingTop ? (
                    <MovieGridSkeleton count={8} />
                ) : (
                    <div className="movie-grid">
                        {topRated?.content?.map(movie => (
                            <MovieCard key={movie.id} movie={movie} />
                        ))}
                    </div>
                )}
            </section>
        </main>
    );
}

function MovieGridSkeleton({ count }) {
    return (
        <div className="movie-grid" aria-busy="true" aria-label="Loading movies">
            {Array.from({ length: count }).map((_, i) => (
                <div key={i} className="movie-card movie-card--skeleton" aria-hidden="true" />
            ))}
        </div>
    );
}

export default HomePage;
