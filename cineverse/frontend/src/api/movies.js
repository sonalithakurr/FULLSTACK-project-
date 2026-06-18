import apiClient from './client';

export const moviesApi = {
    getMovies: (page = 0, size = 20, sortBy = 'releaseDate') =>
        apiClient.get('/api/v1/movies', { params: { page, size, sortBy } }),

    getMovie: (id) =>
        apiClient.get(`/api/v1/movies/${id}`),

    searchMovies: (query, page = 0, size = 10) =>
        apiClient.get('/api/v1/movies/search', { params: { q: query, page, size } }),

    getMoviesByGenre: (slug, page = 0, size = 20) =>
        apiClient.get(`/api/v1/movies/genre/${slug}`, { params: { page, size } }),

    getTopRated: (page = 0, size = 20) =>
        apiClient.get('/api/v1/movies/top-rated', { params: { page, size } }),

    addToWatchlist: (movieId) =>
        apiClient.post(`/api/v1/movies/${movieId}/watchlist`),

    removeFromWatchlist: (movieId) =>
        apiClient.delete(`/api/v1/movies/${movieId}/watchlist`),

    getWatchlist: () =>
        apiClient.get('/api/v1/movies/watchlist'),
};
