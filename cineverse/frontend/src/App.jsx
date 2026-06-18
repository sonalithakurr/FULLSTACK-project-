import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { Toaster } from 'react-hot-toast';

import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import MovieDetailPage from './pages/MovieDetailPage';
import LoginForm from './components/LoginForm';
import useAuthStore from './store/authStore';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 5 * 60 * 1000,       // 5 minutes
            retry: 1,
        },
    },
});

// Protected route wrapper
function ProtectedRoute({ children }) {
    const { isAuthenticated } = useAuthStore();
    return isAuthenticated ? children : <Navigate to="/login" replace />;
}

function App() {
    return (
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <Navbar />
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/movies" element={<HomePage />} />
                    <Route path="/movies/:id" element={<MovieDetailPage />} />
                    <Route path="/login" element={<LoginForm />} />
                    <Route path="/register" element={
                        <React.Suspense fallback={<div>Loading...</div>}>
                            {/* RegisterPage — same pattern as LoginForm */}
                            <LoginForm />
                        </React.Suspense>
                    } />
                    <Route path="/watchlist" element={
                        <ProtectedRoute>
                            <div>Watchlist Page — coming soon</div>
                        </ProtectedRoute>
                    } />
                    <Route path="*" element={<Navigate to="/" replace />} />
                </Routes>
                <Toaster position="bottom-right" />
            </BrowserRouter>
        </QueryClientProvider>
    );
}

export default App;
