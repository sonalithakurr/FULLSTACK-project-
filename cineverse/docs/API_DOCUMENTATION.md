# CineVerse API Documentation

All requests go through the **API Gateway** at `http://localhost:8080`.

## Authentication

Protected endpoints require:
```
Authorization: Bearer <access_token>
```

---

## Auth Service `/api/v1/auth`

### POST /register
Create a new account.

**Request:**
```json
{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "Secret@123",
  "displayName": "John Doe"
}
```

**Response 201:**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "user": {
    "id": "uuid",
    "username": "johndoe",
    "email": "john@example.com",
    "displayName": "John Doe",
    "roles": ["ROLE_USER"]
  }
}
```

---

### POST /login
Authenticate with email/username + password.

**Request:**
```json
{ "identifier": "johndoe", "password": "Secret@123" }
```
**Response 200:** Same as `/register`

---

### POST /refresh
Exchange refresh token for a new access token.

**Request:**
```json
{ "refreshToken": "eyJhbGc..." }
```

---

### POST /logout *(Protected)*
Revokes the current access token.

**Headers:** `Authorization: Bearer <token>`
**Response:** 204 No Content

---

### GET /validate *(Internal — used by gateway)*
```
GET /api/v1/auth/validate
Authorization: Bearer <token>
```
**Response 200:**
```json
{ "userId": "uuid", "username": "johndoe", "roles": "ROLE_USER", "valid": true }
```

---

## Movie Service `/api/v1/movies`

### GET /movies
List movies with pagination.

**Params:** `page` (0), `size` (20), `sortBy` (releaseDate)

**Response 200:**
```json
{
  "content": [
    {
      "id": "uuid",
      "title": "Inception",
      "releaseDate": "2010-07-16",
      "contentRating": "PG-13",
      "runtimeMinutes": 148,
      "averageRating": 8.8,
      "reviewCount": 1250,
      "posterUrl": "https://...",
      "genres": ["Action", "Sci-Fi"]
    }
  ],
  "totalElements": 500,
  "totalPages": 25,
  "number": 0,
  "size": 20
}
```

---

### GET /movies/{id}
Get full movie details.

**Response 200:**
```json
{
  "id": "uuid",
  "title": "Inception",
  "description": "A thief who steals...",
  "releaseDate": "2010-07-16",
  "contentRating": "PG-13",
  "runtimeMinutes": 148,
  "averageRating": 8.8,
  "reviewCount": 1250,
  "posterUrl": "https://...",
  "backdropUrl": "https://...",
  "trailerUrl": "https://youtube.com/...",
  "genres": ["Action", "Sci-Fi"],
  "cast": [{ "id": "uuid", "name": "Leonardo DiCaprio", "role": "ACTOR" }],
  "directors": [{ "id": "uuid", "name": "Christopher Nolan", "role": "DIRECTOR" }]
}
```

---

### GET /movies/search?q={query}
Full-text search across title and description.

---

### GET /movies/genre/{slug}
Filter by genre. `slug` examples: `action`, `sci-fi`, `comedy`

---

### GET /movies/top-rated
Movies sorted by average rating descending.

---

### POST /movies/{id}/watchlist *(Protected)*
Add movie to the authenticated user's watchlist.

### DELETE /movies/{id}/watchlist *(Protected)*
Remove from watchlist.

### GET /movies/watchlist *(Protected)*
Get current user's watchlist.

---

## Review Service `/api/v1/reviews`

### GET /reviews/movie/{movieId}
Get paginated reviews for a movie.

**Params:** `page` (0), `size` (10)

**Response 200:**
```json
{
  "content": [
    {
      "id": "mongo-id",
      "movieId": "uuid",
      "userId": "uuid",
      "username": "johndoe",
      "userDisplayName": "John Doe",
      "rating": 9,
      "title": "A masterpiece",
      "content": "Nolan outdoes himself...",
      "tags": ["mind-bending"],
      "containsSpoilers": false,
      "helpfulCount": 42,
      "createdAt": "2024-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1250,
  "totalPages": 125
}
```

---

### GET /reviews/movie/{movieId}/summary
```json
{ "avg": 8.5, "count": 1250 }
```

---

### POST /reviews *(Protected)*
Submit a review.

**Request:**
```json
{
  "movieId": "uuid",
  "rating": 9,
  "title": "A masterpiece",
  "content": "Nolan outdoes himself with...",
  "tags": ["mind-bending", "must-rewatch"],
  "containsSpoilers": false
}
```

**Validation:**
- `rating`: 1–10
- `title`: max 150 chars
- `content`: 10–5000 chars

---

### PUT /reviews/{id} *(Protected)*
Update your own review. Same request body as POST.

---

### POST /reviews/{id}/helpful *(Protected)*
Mark a review as helpful (idempotent).

---

## Error Response Format (RFC 7807)

All errors follow Problem Detail standard:
```json
{
  "type": "/errors/authentication",
  "title": "Authentication Failed",
  "status": 401,
  "detail": "Invalid credentials",
  "instance": "/api/v1/auth/login"
}
```

For validation errors:
```json
{
  "type": "/errors/validation",
  "title": "Validation Failed",
  "status": 400,
  "fields": {
    "email": "Invalid email format",
    "password": "Password must contain uppercase, lowercase and digit"
  }
}
```
