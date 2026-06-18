import apiClient from './client';

export const authApi = {
    register: (data) =>
        apiClient.post('/api/v1/auth/register', data),

    login: (identifier, password) =>
        apiClient.post('/api/v1/auth/login', { identifier, password }),

    logout: () =>
        apiClient.post('/api/v1/auth/logout'),

    refresh: (refreshToken) =>
        apiClient.post('/api/v1/auth/refresh', { refreshToken }),
};
