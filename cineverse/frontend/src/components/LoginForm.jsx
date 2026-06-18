import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import useAuthStore from '../store/authStore';

function LoginForm() {
    const [identifier, setIdentifier] = useState('');
    const [password, setPassword] = useState('');
    const { login, isLoading, error, clearError } = useAuthStore();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        clearError();
        const result = await login(identifier, password);
        if (result.success) {
            navigate('/');
        }
    };

    return (
        <main className="auth-page" aria-labelledby="login-heading">
            <div className="auth-card">
                <h1 id="login-heading" className="auth-card__title">Sign In</h1>
                <p className="auth-card__subtitle">Welcome back to CineVerse</p>

                <form onSubmit={handleSubmit} noValidate>
                    {error && (
                        <div className="auth-card__error" role="alert" aria-live="polite">
                            {error}
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="identifier" className="form-label">
                            Email or Username
                        </label>
                        <input
                            id="identifier"
                            type="text"
                            className="form-input"
                            value={identifier}
                            onChange={(e) => setIdentifier(e.target.value)}
                            placeholder="you@example.com"
                            required
                            autoComplete="username"
                            aria-required="true"
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password" className="form-label">
                            Password
                        </label>
                        <input
                            id="password"
                            type="password"
                            className="form-input"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            placeholder="••••••••"
                            required
                            autoComplete="current-password"
                            aria-required="true"
                        />
                    </div>

                    <button
                        type="submit"
                        className="btn btn--primary btn--full"
                        disabled={isLoading || !identifier || !password}
                        aria-busy={isLoading}
                    >
                        {isLoading ? 'Signing in...' : 'Sign In'}
                    </button>
                </form>

                <p className="auth-card__footer">
                    Don't have an account?{' '}
                    <Link to="/register" className="auth-card__link">
                        Create one
                    </Link>
                </p>
            </div>
        </main>
    );
}

export default LoginForm;
