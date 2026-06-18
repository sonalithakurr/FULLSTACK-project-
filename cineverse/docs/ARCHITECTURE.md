# CineVerse Architecture

## System Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────┐
│                          USER LAYER                                   │
│                                                                       │
│   Browser (React SPA)          Mobile (future)                       │
│   http://localhost:3000                                               │
└────────────────────────────────┬─────────────────────────────────────┘
                                 │ HTTPS
┌────────────────────────────────▼─────────────────────────────────────┐
│                    API GATEWAY (Port 8080)                            │
│                                                                       │
│   ┌─────────────┐  ┌───────────────┐  ┌────────────────────────┐    │
│   │ JWT Filter  │  │ Rate Limiter  │  │  Request Router        │    │
│   │ (validates  │  │ (Redis-backed │  │  /api/v1/auth  → 8081 │    │
│   │  token +    │  │  per IP/user) │  │  /api/v1/movies → 8082 │   │
│   │  blacklist) │  └───────────────┘  │  /api/v1/reviews→ 8083 │   │
│   │             │                     └────────────────────────┘    │
│   │ Injects:    │                                                     │
│   │ X-User-Id   │                                                     │
│   │ X-Username  │                                                     │
│   │ X-Roles     │                                                     │
│   └─────────────┘                                                     │
└──────────┬───────────────────┬────────────────────┬──────────────────┘
           │                   │                    │
┌──────────▼───┐    ┌──────────▼───┐    ┌──────────▼────────┐
│ AUTH SERVICE │    │MOVIE SERVICE │    │  REVIEW SERVICE   │
│  Port 8081   │    │  Port 8082   │    │   Port 8083       │
│              │    │              │    │                   │
│ - Register   │    │ - List/Search│    │ - Create review   │
│ - Login      │    │ - Movie CRUD │    │ - Get reviews     │
│ - Refresh    │    │ - Watchlist  │    │ - Rating summary  │
│ - Validate   │    │ - Top rated  │    │ - Mark helpful    │
└──────┬───────┘    └──────┬───────┘    └─────────┬─────────┘
       │                   │                      │
┌──────▼───────┐    ┌──────▼───────┐    ┌─────────▼─────────┐
│  PostgreSQL  │    │  PostgreSQL  │    │     MongoDB       │
│  auth_db     │    │  movies_db   │    │   reviews_db      │
│  Port 5432   │    │  Port 5433   │    │   Port 27017      │
│              │    │              │    │                   │
│  users       │    │  movies      │    │  reviews          │
│  user_roles  │    │  genres      │    │  (flexible docs)  │
└──────────────┘    │  persons     │    └───────────────────┘
                    │  movie_genres│
                    │  watchlist   │
                    └──────┬───────┘
                           │
              ┌────────────▼──────────────┐
              │          Redis            │
              │        Port 6379          │
              │                           │
              │  Cache (movies, reviews)  │
              │  JWT Blacklist            │
              │  Refresh Token Store      │
              │  Rate Limiter Counters    │
              └───────────────────────────┘

              ┌────────────────────────────────────────┐
              │              RabbitMQ                  │
              │             Port 5672                  │
              │                                        │
              │  Exchange: cineverse.users (topic)     │
              │    └─ user.registered → user.registered│
              │         .queue                         │
              │                                        │
              │  Exchange: cineverse.reviews (topic)   │
              │    └─ review.rating.updated →          │
              │         review.rating.updated.queue    │
              └────────────────────────────────────────┘
```

## Data Flow: User Registers

```
1. POST /api/v1/auth/register
   → API Gateway (rate limit check)
   → Auth Service
   → Hash password (BCrypt 12)
   → Save user to PostgreSQL
   → Generate JWT (access + refresh)
   → Store refresh token in Redis
   → Publish USER_REGISTERED event to RabbitMQ (async)
   → Return 201 with tokens

2. RabbitMQ delivers USER_REGISTERED
   → Movie Service consumes event
   → Creates default watchlist entry
```

## Data Flow: Browse Movies

```
1. GET /api/v1/movies?page=0
   → API Gateway (no JWT required for GET)
   → Movie Service
   → Check Redis cache (key: "movies:0-20-releaseDate")
   → Cache HIT: return cached response (< 1ms)
   → Cache MISS: query PostgreSQL, cache result (TTL: 10min)
   → Return paginated movies
```

## Data Flow: Submit Review

```
1. POST /api/v1/reviews
   → API Gateway
   → JWT Filter: validate token, check Redis blacklist
   → Inject X-User-Id, X-Username headers
   → Review Service
   → Validate: one review per user per movie (MongoDB unique index)
   → Save review to MongoDB
   → Compute new average rating (MongoDB aggregation)
   → Publish RATING_UPDATED event (async)
   → Return 201

2. RabbitMQ delivers RATING_UPDATED
   → Movie Service evicts movie cache (Redis)
   → Next request to movie detail fetches fresh data
```

## Scalability Design

Each service can be horizontally scaled independently:
- Auth Service: stateless JWT validation scales linearly
- Movie Service: Redis cache absorbs read spikes
- Review Service: MongoDB handles write-heavy workloads

Single points of failure mitigated:
- Redis: persistence enabled, can use Redis Cluster in prod
- RabbitMQ: durable queues + DLX for failed messages
- PostgreSQL: primary/replica replication in prod
- MongoDB: replica set in prod
