import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi } from '../api/auth';

/**
 * Auth store using Zustand with localStorage persistence.
 *
 * State is persisted so users stay logged in after page refresh.
 * Sensitive data (passwords) never touch this store.
 */
const useAuthStore = create(
    persist(
        (set, get) => ({
            user: null,
            accessToken: null,
            refreshToken: null,
            isAuthenticated: false,
            isLoading: false,
            error: null,

            login: async (identifier, password) => {
                set({ isLoading: true, error: null });
                try {
                    const { data } = await authApi.login(identifier, password);
                    localStorage.setItem('accessToken', data.accessToken);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    set({
                        user: data.user,
                        accessToken: data.accessToken,
                        refreshToken: data.refreshToken,
                        isAuthenticated: true,
                        isLoading: false,
                    });
                    return { success: true };
                } catch (err) {
                    const message = err.response?.data?.detail || 'Login failed';
                    set({ error: message, isLoading: false });
                    return { success: false, error: message };
                }
            },

            register: async (userData) => {
                set({ isLoading: true, error: null });
                try {
                    const { data } = await authApi.register(userData);
                    localStorage.setItem('accessToken', data.accessToken);
                    localStorage.setItem('refreshToken', data.refreshToken);
                    set({
                        user: data.user,
                        accessToken: data.accessToken,
                        refreshToken: data.refreshToken,
                        isAuthenticated: true,
                        isLoading: false,
                    });
                    return { success: true };
                } catch (err) {
                    const message = err.response?.data?.detail || 'Registration failed';
                    set({ error: message, isLoading: false });
                    return { success: false, error: message };
                }
            },

            logout: async () => {
                try {
                    await authApi.logout();
                } catch {
                    // Proceed with local cleanup even if server call fails
                } finally {
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                    set({
                        user: null,
                        accessToken: null,
                        refreshToken: null,
                        isAuthenticated: false,
                    });
                }
            },

            clearError: () => set({ error: null }),
        }),
        {
            name: 'cineverse-auth',
            partialize: (state) => ({
                user: state.user,
                isAuthenticated: state.isAuthenticated,
            }),
        }
    )
);

export default useAuthStore;
