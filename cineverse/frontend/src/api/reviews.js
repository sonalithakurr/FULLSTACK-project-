import apiClient from './client';

export const reviewsApi = {
    getReviews: (movieId, page = 0, size = 10) =>
        apiClient.get(`/api/v1/reviews/movie/${movieId}`, { params: { page, size } }),

    getRatingSummary: (movieId) =>
        apiClient.get(`/api/v1/reviews/movie/${movieId}/summary`),

    createReview: (data) =>
        apiClient.post('/api/v1/reviews', data),

    updateReview: (id, data) =>
        apiClient.put(`/api/v1/reviews/${id}`, data),

    markHelpful: (reviewId) =>
        apiClient.post(`/api/v1/reviews/${reviewId}/helpful`),
};
