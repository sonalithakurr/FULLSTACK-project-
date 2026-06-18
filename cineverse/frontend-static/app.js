// ═══════════════════════════════════════════════════════════
//  CineVerse — Application Logic
// ═══════════════════════════════════════════════════════════

// ─── State ────────────────────────────────────────────────
let state = {
  user: JSON.parse(localStorage.getItem('cv_user') || 'null'),
  watchlist: JSON.parse(localStorage.getItem('cv_watchlist') || '[]'),
  userReviews: JSON.parse(localStorage.getItem('cv_reviews') || '[]'),
  currentMovie: null,
  currentRating: 0,
  visibleCount: 8,
  activeGenre: 'All',
  filteredMovies: [...MOVIES]
};

// ─── Init ─────────────────────────────────────────────────
window.addEventListener('DOMContentLoaded', () => {
  updateNavAuth();
  renderMovieGrid();
  renderTopRated();
  showPage('home');
});

// ─── Page Router ──────────────────────────────────────────
function showPage(pageId) {
  document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
  const page = document.getElementById('page-' + pageId);
  if (page) {
    page.classList.add('active');
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }
  if (pageId === 'watchlist') renderWatchlist();
}

// ─── Auth ─────────────────────────────────────────────────
function handleLogin(e) {
  e.preventDefault();
  const id = document.getElementById('loginId').value.trim();
  const pass = document.getElementById('loginPass').value;
  const errEl = document.getElementById('loginError');

  if (!id || !pass) { showAuthError(errEl, 'Please fill in all fields'); return; }
  if (pass.length < 3) { showAuthError(errEl, 'Invalid credentials'); return; }

  // Simulate login (demo mode — accepts any credentials)
  const user = { id: 'u_' + Date.now(), username: id.includes('@') ? id.split('@')[0] : id, displayName: id.includes('@') ? id.split('@')[0] : id, email: id.includes('@') ? id : id + '@demo.com' };
  loginUser(user);
}

function handleRegister(e) {
  e.preventDefault();
  const username = document.getElementById('regUsername').value.trim();
  const display = document.getElementById('regDisplay').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const pass = document.getElementById('regPass').value;
  const errEl = document.getElementById('registerError');
  const successEl = document.getElementById('registerSuccess');

  if (!username || !display || !email || !pass) { showAuthError(errEl, 'Please fill in all fields'); return; }
  if (pass.length < 8) { showAuthError(errEl, 'Password must be at least 8 characters'); return; }
  if (!/[A-Z]/.test(pass)) { showAuthError(errEl, 'Password must contain an uppercase letter'); return; }
  if (!/[0-9]/.test(pass)) { showAuthError(errEl, 'Password must contain a number'); return; }

  errEl.classList.add('hidden');
  successEl.textContent = '🎉 Account created! Welcome to CineVerse!';
  successEl.classList.remove('hidden');

  const user = { id: 'u_' + Date.now(), username, displayName: display, email };
  setTimeout(() => loginUser(user), 800);
}

function demoLogin() {
  const user = { id: 'demo_1', username: 'demo_user', displayName: 'Demo User', email: 'demo@cineverse.com' };
  loginUser(user);
}

function loginUser(user) {
  state.user = user;
  localStorage.setItem('cv_user', JSON.stringify(user));
  updateNavAuth();
  showPage('home');
  showToast('👋 Welcome back, ' + user.displayName + '!', 'success');
}

function logout() {
  state.user = null;
  localStorage.removeItem('cv_user');
  updateNavAuth();
  showPage('home');
  showToast('You\'ve been signed out', 'info');
}

function updateNavAuth() {
  const navAuth = document.getElementById('navAuth');
  if (state.user) {
    navAuth.innerHTML = `
      <div class="user-menu">
        <button class="btn btn-ghost user-btn" onclick="toggleUserMenu()">
          <div class="avatar">${state.user.displayName[0].toUpperCase()}</div>
          <span>${state.user.displayName}</span>
          <span class="chevron">▾</span>
        </button>
        <div class="user-dropdown hidden" id="userDropdown">
          <div class="dropdown-header">
            <strong>${state.user.displayName}</strong>
            <small>${state.user.email}</small>
          </div>
          <a href="#" onclick="showPage('watchlist');closeUserMenu()">🔖 My Watchlist (${state.watchlist.length})</a>
          <a href="#" onclick="logout()">🚪 Sign Out</a>
        </div>
      </div>`;
  } else {
    navAuth.innerHTML = `
      <a href="#" class="btn btn-ghost" onclick="showPage('login')">Sign In</a>
      <a href="#" class="btn btn-primary" onclick="showPage('register')">Join Free</a>`;
  }
}

function toggleUserMenu() {
  document.getElementById('userDropdown')?.classList.toggle('hidden');
}

function closeUserMenu() {
  document.getElementById('userDropdown')?.classList.add('hidden');
}

// Close dropdown when clicking outside
document.addEventListener('click', (e) => {
  if (!e.target.closest('.user-menu')) closeUserMenu();
});

function showAuthError(el, msg) {
  el.textContent = '⚠️ ' + msg;
  el.classList.remove('hidden');
}

// ─── Movie Grid ───────────────────────────────────────────
function renderMovieGrid() {
  const grid = document.getElementById('movieGrid');
  const countEl = document.getElementById('movieCount');
  const movies = state.filteredMovies.slice(0, state.visibleCount);

  countEl.textContent = state.filteredMovies.length + ' movies';
  grid.innerHTML = movies.map(m => movieCardHTML(m)).join('');

  const loadBtn = document.getElementById('loadMoreBtn');
  loadBtn.style.display = state.visibleCount >= state.filteredMovies.length ? 'none' : 'block';
}

function renderTopRated() {
  const grid = document.getElementById('topRatedGrid');
  const top = [...MOVIES].sort((a, b) => b.averageRating - a.averageRating).slice(0, 4);
  grid.innerHTML = top.map(m => movieCardHTML(m)).join('');
}

function movieCardHTML(movie) {
  const inWL = state.watchlist.includes(movie.id);
  const rating = movie.averageRating ? Number(movie.averageRating).toFixed(1) : null;
  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : 'TBA';
  const genreStr = movie.genres?.slice(0, 2).join(' · ') || '';

  return `
    <article class="movie-card" onclick="openMovie('${movie.id}')">
      <div class="card-poster">
        <img src="${movie.posterUrl}" alt="${movie.title}" loading="lazy" onerror="this.src='https://via.placeholder.com/300x450/1a1a2e/e5e5e5?text=${encodeURIComponent(movie.title)}'"/>
        <div class="card-overlay">
          <span class="card-genres">${genreStr}</span>
        </div>
        <button class="wl-btn ${inWL ? 'active' : ''}" onclick="toggleWatchlist(event,'${movie.id}')" title="${inWL ? 'Remove from watchlist' : 'Add to watchlist'}">
          ${inWL ? '🔖' : '🔕'}
        </button>
        ${rating ? `<div class="card-badge">${rating}</div>` : ''}
      </div>
      <div class="card-body">
        <h3 class="card-title">${movie.title}</h3>
        <div class="card-meta">
          <span>${year}</span>
          ${movie.contentRating ? `<span class="content-badge">${movie.contentRating}</span>` : ''}
          ${movie.runtimeMinutes ? `<span>${movie.runtimeMinutes}m</span>` : ''}
        </div>
      </div>
    </article>`;
}

function loadMore() {
  state.visibleCount += 8;
  renderMovieGrid();
}

// ─── Genre Filter ─────────────────────────────────────────
function filterGenre(genre, btn) {
  state.activeGenre = genre;
  state.visibleCount = 8;

  document.querySelectorAll('.genre-pill').forEach(p => p.classList.remove('active'));
  btn.classList.add('active');

  state.filteredMovies = genre === 'All'
    ? [...MOVIES]
    : MOVIES.filter(m => m.genres?.includes(genre));

  renderMovieGrid();
}

// ─── Search ───────────────────────────────────────────────
function handleSearch(e) {
  e.preventDefault();
  const q = document.getElementById('searchInput').value.trim().toLowerCase();
  if (!q) return;

  const results = MOVIES.filter(m =>
    m.title.toLowerCase().includes(q) ||
    m.description?.toLowerCase().includes(q) ||
    m.genres?.some(g => g.toLowerCase().includes(q)) ||
    m.cast?.some(c => c.toLowerCase().includes(q))
  );

  showPage('search');
  const grid = document.getElementById('searchGrid');
  const countEl = document.getElementById('searchResultCount');
  const emptyEl = document.getElementById('searchEmpty');

  countEl.textContent = `"${q}" — ${results.length} result${results.length !== 1 ? 's' : ''}`;

  if (results.length === 0) {
    grid.innerHTML = '';
    emptyEl.classList.remove('hidden');
  } else {
    emptyEl.classList.add('hidden');
    grid.innerHTML = results.map(m => movieCardHTML(m)).join('');
  }

  document.getElementById('searchInput').value = '';
}

// ─── Top Rated Page ───────────────────────────────────────
function showTopRated() {
  // Re-use home page but scroll to top strip
  showPage('home');
  setTimeout(() => {
    document.querySelector('.top-strip')?.scrollIntoView({ behavior: 'smooth' });
  }, 100);
}

// ─── Movie Detail ─────────────────────────────────────────
function openMovie(id) {
  const movie = MOVIES.find(m => m.id === id);
  if (!movie) return;
  state.currentMovie = movie;

  const reviews = [...(REVIEWS[id] || []), ...state.userReviews.filter(r => r.movieId === id)];
  const inWL = state.watchlist.includes(id);

  document.getElementById('detailContent').innerHTML = detailHTML(movie, reviews, inWL);
  showPage('detail');
}

function detailHTML(movie, reviews, inWL) {
  const year = movie.releaseDate ? new Date(movie.releaseDate).getFullYear() : 'TBA';
  const rating = movie.averageRating ? Number(movie.averageRating).toFixed(1) : 'N/A';
  const hours = movie.runtimeMinutes ? Math.floor(movie.runtimeMinutes / 60) + 'h ' + (movie.runtimeMinutes % 60) + 'm' : '';
  const genrePills = movie.genres?.map(g => `<span class="pill">${g}</span>`).join('') || '';
  const castList = movie.cast?.slice(0, 6).map(c => `<span class="cast-chip">${c}</span>`).join('') || '';
  const dirName = movie.directors?.map(d => d.name).join(', ') || 'Unknown';

  const reviewsHTML = reviews.length
    ? reviews.map(r => reviewCardHTML(r)).join('')
    : `<div class="empty-state"><div class="empty-icon">📝</div><p>No reviews yet. Be the first!</p></div>`;

  const canReview = state.user && !state.userReviews.find(r => r.movieId === movie.id);

  return `
    <div class="detail-backdrop" style="background-image:url('${movie.backdropUrl || ''}')"></div>

    <div class="detail-wrap container">
      <button class="back-btn" onclick="showPage('home')">← Back</button>

      <div class="detail-layout">
        <!-- Poster -->
        <aside class="detail-poster-wrap">
          <img class="detail-poster" src="${movie.posterUrl}" alt="${movie.title}" onerror="this.style.display='none'"/>
          <div class="poster-actions">
            <button class="btn ${inWL ? 'btn-primary' : 'btn-ghost'} btn-full" onclick="toggleWatchlistDirect('${movie.id}', this)">
              ${inWL ? '🔖 In Watchlist' : '+ Add to Watchlist'}
            </button>
            ${movie.trailerUrl ? `<a href="${movie.trailerUrl}" target="_blank" rel="noopener" class="btn btn-ghost btn-full">▶ Watch Trailer</a>` : ''}
          </div>
        </aside>

        <!-- Info -->
        <article class="detail-info">
          <div class="detail-genres">${genrePills}</div>
          <h1 class="detail-title">${movie.title}</h1>

          <div class="detail-stats">
            <div class="stat-box">
              <span class="stat-val golden">★ ${rating}</span>
              <span class="stat-lbl">${movie.reviewCount ? movie.reviewCount.toLocaleString() + ' reviews' : 'Rating'}</span>
            </div>
            <div class="stat-box">
              <span class="stat-val">${year}</span>
              <span class="stat-lbl">Released</span>
            </div>
            ${hours ? `<div class="stat-box"><span class="stat-val">${hours}</span><span class="stat-lbl">Runtime</span></div>` : ''}
            ${movie.contentRating ? `<div class="stat-box"><span class="stat-val">${movie.contentRating}</span><span class="stat-lbl">Rating</span></div>` : ''}
          </div>

          <p class="detail-desc">${movie.description}</p>

          <div class="detail-crew">
            <span class="crew-label">Director</span>
            <span class="crew-name">${dirName}</span>
          </div>

          ${movie.cast?.length ? `
          <div class="detail-cast">
            <h3 class="section-sm">Cast</h3>
            <div class="cast-chips">${castList}</div>
          </div>` : ''}

          <!-- Review CTA -->
          <div class="review-cta">
            ${state.user
              ? canReview
                ? `<button class="btn btn-primary btn-lg" onclick="openReviewModal('${movie.id}')">✍ Write a Review</button>`
                : `<span class="reviewed-badge">✅ You reviewed this movie</span>`
              : `<p class="auth-prompt">
                  <a href="#" onclick="showPage('login')">Sign in</a> to write a review
                </p>`
            }
          </div>
        </article>
      </div>

      <!-- Reviews -->
      <section class="reviews-section">
        <div class="reviews-header">
          <h2>Reviews <span class="review-count">${reviews.length}</span></h2>
        </div>
        <div class="reviews-list">${reviewsHTML}</div>
      </section>
    </div>`;
}

function reviewCardHTML(r) {
  const stars = '★'.repeat(r.rating) + '☆'.repeat(10 - r.rating);
  return `
    <article class="review-card">
      <div class="review-top">
        <div class="reviewer">
          <div class="reviewer-avatar">${r.displayName[0]}</div>
          <div>
            <strong>${r.displayName}</strong>
            <small>@${r.username}</small>
          </div>
        </div>
        <div class="review-rating">
          <span class="stars-display golden">${stars}</span>
          <span class="rating-num">${r.rating}/10</span>
        </div>
      </div>
      ${r.spoiler ? '<span class="spoiler-badge">⚠ Contains spoilers</span>' : ''}
      <h4 class="review-title">${r.title}</h4>
      <p class="review-body">${r.content}</p>
      <div class="review-footer">
        ${r.tags?.map(t => `<span class="tag">#${t}</span>`).join('') || ''}
        <span class="helpful-count">👍 ${r.helpful} found helpful</span>
        ${r.date ? `<span class="review-date">${r.date}</span>` : ''}
      </div>
    </article>`;
}

// ─── Watchlist ────────────────────────────────────────────
function toggleWatchlist(e, movieId) {
  e.stopPropagation();
  if (!state.user) { showToast('Please sign in to use your watchlist', 'warning'); showPage('login'); return; }

  const idx = state.watchlist.indexOf(movieId);
  if (idx === -1) {
    state.watchlist.push(movieId);
    showToast('🔖 Added to watchlist!', 'success');
  } else {
    state.watchlist.splice(idx, 1);
    showToast('Removed from watchlist', 'info');
  }

  localStorage.setItem('cv_watchlist', JSON.stringify(state.watchlist));
  updateNavAuth();
  renderMovieGrid();
  renderTopRated();
  if (document.getElementById('page-search').classList.contains('active')) handleSearchRerender();
}

function toggleWatchlistDirect(movieId, btn) {
  if (!state.user) { showToast('Please sign in to use your watchlist', 'warning'); showPage('login'); return; }

  const idx = state.watchlist.indexOf(movieId);
  if (idx === -1) {
    state.watchlist.push(movieId);
    btn.className = 'btn btn-primary btn-full';
    btn.textContent = '🔖 In Watchlist';
    showToast('🔖 Added to watchlist!', 'success');
  } else {
    state.watchlist.splice(idx, 1);
    btn.className = 'btn btn-ghost btn-full';
    btn.textContent = '+ Add to Watchlist';
    showToast('Removed from watchlist', 'info');
  }

  localStorage.setItem('cv_watchlist', JSON.stringify(state.watchlist));
  updateNavAuth();
}

function renderWatchlist() {
  const grid = document.getElementById('watchlistGrid');
  const empty = document.getElementById('watchlistEmpty');
  const count = document.getElementById('watchlistCount');

  const movies = MOVIES.filter(m => state.watchlist.includes(m.id));
  count.textContent = movies.length + ' movie' + (movies.length !== 1 ? 's' : '');

  if (movies.length === 0) {
    grid.innerHTML = '';
    empty.classList.remove('hidden');
  } else {
    empty.classList.add('hidden');
    grid.innerHTML = movies.map(m => movieCardHTML(m)).join('');
  }
}

// ─── Review Modal ─────────────────────────────────────────
function openReviewModal(movieId) {
  state.currentMovie = MOVIES.find(m => m.id === movieId);
  state.currentRating = 0;
  document.getElementById('reviewModalTitle').textContent = 'Review: ' + state.currentMovie?.title;
  document.getElementById('reviewTitle').value = '';
  document.getElementById('reviewContent').value = '';
  document.getElementById('charCount').textContent = '0 / 2000';
  document.getElementById('spoilerCheck').checked = false;
  document.getElementById('ratingDisplay').textContent = 'Select rating';
  document.querySelectorAll('.star').forEach(s => s.classList.remove('active'));
  document.getElementById('reviewModal').classList.remove('hidden');

  document.getElementById('reviewContent').addEventListener('input', function() {
    document.getElementById('charCount').textContent = this.value.length + ' / 2000';
  });
}

function closeReviewModal() {
  document.getElementById('reviewModal').classList.add('hidden');
}

function setRating(val) {
  state.currentRating = val;
  document.getElementById('ratingDisplay').textContent = val + '/10';
  document.querySelectorAll('.star').forEach(s => {
    s.classList.toggle('active', parseInt(s.dataset.val) <= val);
  });
}

function submitReview(e) {
  e.preventDefault();
  if (!state.currentRating) { showToast('Please select a rating', 'warning'); return; }

  const review = {
    id: 'rev_' + Date.now(),
    movieId: state.currentMovie.id,
    username: state.user.username,
    displayName: state.user.displayName,
    rating: state.currentRating,
    title: document.getElementById('reviewTitle').value,
    content: document.getElementById('reviewContent').value,
    spoiler: document.getElementById('spoilerCheck').checked,
    helpful: 0,
    tags: [],
    date: new Date().toISOString().split('T')[0]
  };

  state.userReviews.push(review);
  localStorage.setItem('cv_reviews', JSON.stringify(state.userReviews));

  closeReviewModal();
  showToast('✅ Review submitted! Thanks for sharing.', 'success');

  // Re-render detail page to show new review
  if (state.currentMovie) openMovie(state.currentMovie.id);
}

// ─── Password Strength ────────────────────────────────────
const regPassEl = document.getElementById('regPass');
if (regPassEl) {
  regPassEl.addEventListener('input', function() {
    const pw = this.value;
    let score = 0;
    let msg = '';
    if (pw.length >= 8) score++;
    if (/[A-Z]/.test(pw)) score++;
    if (/[0-9]/.test(pw)) score++;
    if (/[^a-zA-Z0-9]/.test(pw)) score++;

    const labels = ['', 'Weak', 'Fair', 'Good', 'Strong'];
    const colors = ['', '#e50914', '#f59e0b', '#3b82f6', '#22c55e'];
    const el = document.getElementById('passStrength');
    if (el && pw.length > 0) {
      el.innerHTML = `<div class="strength-bar"><div style="width:${score*25}%;background:${colors[score]}"></div></div><span style="color:${colors[score]}">${labels[score]}</span>`;
    }
  });
}

// ─── Password Toggle ──────────────────────────────────────
function togglePass(inputId, btn) {
  const input = document.getElementById(inputId);
  if (input.type === 'password') {
    input.type = 'text';
    btn.textContent = '🙈';
  } else {
    input.type = 'password';
    btn.textContent = '👁';
  }
}

// ─── Toast ────────────────────────────────────────────────
function showToast(message, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  container.appendChild(toast);
  setTimeout(() => toast.classList.add('show'), 10);
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}

// ─── Helpers ──────────────────────────────────────────────
function handleSearchRerender() {
  const q = document.getElementById('searchInput').value.trim().toLowerCase();
  if (q) handleSearch(new Event('submit'));
}
