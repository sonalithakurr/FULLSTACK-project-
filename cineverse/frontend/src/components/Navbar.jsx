import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Search, Film, User, LogOut, Bookmark } from 'lucide-react';
import useAuthStore from '../store/authStore';

function Navbar() {
    const { isAuthenticated, user, logout } = useAuthStore();
    const [searchQuery, setSearchQuery] = useState('');
    const navigate = useNavigate();

    const handleSearch = (e) => {
        e.preventDefault();
        if (searchQuery.trim()) {
            navigate(`/search?q=${encodeURIComponent(searchQuery.trim())}`);
            setSearchQuery('');
        }
    };

    const handleLogout = async () => {
        await logout();
        navigate('/');
    };

    return (
        <header className="navbar" role="banner">
            <nav className="navbar__inner" aria-label="Main navigation">
                {/* Logo */}
                <Link to="/" className="navbar__logo" aria-label="CineVerse home">
                    <Film size={28} aria-hidden="true" />
                    <span>CineVerse</span>
                </Link>

                {/* Search */}
                <form
                    className="navbar__search"
                    onSubmit={handleSearch}
                    role="search"
                    aria-label="Search movies"
                >
                    <label htmlFor="nav-search" className="sr-only">
                        Search movies
                    </label>
                    <Search
                        size={16}
                        className="navbar__search-icon"
                        aria-hidden="true"
                    />
                    <input
                        id="nav-search"
                        type="search"
                        className="navbar__search-input"
                        placeholder="Search movies..."
                        value={searchQuery}
                        onChange={(e) => setSearchQuery(e.target.value)}
                        aria-label="Search movies"
                    />
                </form>

                {/* Nav Links */}
                <ul className="navbar__links" role="list">
                    <li>
                        <Link to="/movies" className="navbar__link">Movies</Link>
                    </li>
                    <li>
                        <Link to="/movies/top-rated" className="navbar__link">Top Rated</Link>
                    </li>
                </ul>

                {/* Auth Section */}
                <div className="navbar__auth">
                    {isAuthenticated ? (
                        <>
                            <Link
                                to="/watchlist"
                                className="navbar__icon-btn"
                                aria-label="My Watchlist"
                            >
                                <Bookmark size={20} aria-hidden="true" />
                            </Link>
                            <button
                                className="navbar__user-btn"
                                aria-label={`Signed in as ${user?.displayName}`}
                            >
                                <User size={20} aria-hidden="true" />
                                <span>{user?.displayName}</span>
                            </button>
                            <button
                                className="navbar__icon-btn"
                                onClick={handleLogout}
                                aria-label="Sign out"
                            >
                                <LogOut size={20} aria-hidden="true" />
                            </button>
                        </>
                    ) : (
                        <>
                            <Link to="/login" className="btn btn--ghost">
                                Sign In
                            </Link>
                            <Link to="/register" className="btn btn--primary">
                                Join Free
                            </Link>
                        </>
                    )}
                </div>
            </nav>
        </header>
    );
}

export default Navbar;
