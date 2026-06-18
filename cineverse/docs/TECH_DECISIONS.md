# Technology Decisions

Justification for each technology choice in CineVerse.

---

## Spring Boot (Backend Microservices)

**Why:** Production-grade Java framework with auto-configuration, built-in security, and a rich ecosystem. The Spring ecosystem covers everything we need — JPA, Security, AMQP, Redis — with consistent abstractions. Spring Cloud Gateway is the natural choice for the API Gateway when services are Spring Boot based.

**Alternatives considered:** Node.js/Express (better for I/O-bound services, but Java's type safety and Spring Security's mature auth support won out for a security-critical system), Quarkus (smaller footprint but smaller ecosystem for our use case).

---

## PostgreSQL (Structured Data)

**Why:**
- **Auth data** (users, roles) requires ACID transactions — you can't afford partial writes when creating a user + their roles.
- **Movie data** has clear relational structure (movies → genres, movies → cast) that benefits from JOINs and referential integrity.
- Full-text search support via `tsvector`/`GIN` indexes covers our search requirements without needing a separate search engine.

**Schema design choice:** Two separate PostgreSQL instances (one per service) enforce the microservices principle — each service owns its data. The auth-service can be scaled or replaced independently.

---

## MongoDB (Reviews)

**Why:**
- Reviews have a variable structure — tags, spoiler flags, nested helpfulness votes — that would require many nullable columns or complex join tables in SQL.
- Write patterns are append-heavy (new reviews > edits).
- Aggregation pipelines make per-movie rating calculations straightforward.
- Horizontal sharding (by `movieId`) is natural if review volume explodes.

**Data modeling choice:** `helpfulVoterIds` is embedded in the review document. This is a deliberate denormalization — it makes "did I find this helpful?" checks O(1) without a join. Acceptable because the array is bounded (users stop voting after a while).

---

## Redis (Caching)

**Why:**
- Sub-millisecond reads for hot data (popular movie lists, top-rated).
- Built-in TTL support cleanly handles cache invalidation without custom logic.
- Pub/Sub and rate-limiting data structures are native.
- Used for three distinct purposes: response caching, JWT blacklisting, and rate limiter state.

**Cache invalidation strategy:** Event-driven via RabbitMQ. When a review is submitted, a `RATING_UPDATED` event triggers cache eviction in the movie-service. This avoids stale ratings while keeping services decoupled.

---

## RabbitMQ (Message Broker)

**Why:**
- Decouples services — the review-service doesn't need to know the movie-service's URL to trigger a cache eviction.
- Durable queues ensure events survive broker restarts.
- Manual message acknowledgment lets us implement reliable processing with dead-letter queues for failed messages.
- Topic exchanges with routing keys give us flexible publish/subscribe patterns.

**Alternative:** Kafka would be better at higher scale (millions of events/day) with its log-based retention. For CineVerse's scale, RabbitMQ's simpler operational model wins.

---

## JWT (Authentication)

**Why stateless JWT over session cookies:**
- Sessions require a shared session store (another Redis cluster or sticky routing) — adds operational complexity.
- JWT works naturally across horizontal pod replicas with zero shared state.
- The API Gateway can validate tokens locally without calling the auth-service on every request.

**Why HMAC-SHA256 over RSA:**
- In a closed microservices system, all services share the same secret — asymmetric keys aren't necessary.
- RSA would only add value if external third parties needed to verify our tokens.

**Token blacklisting trade-off:** Pure JWT is stateless but can't be revoked. We solve this by storing revoked tokens in Redis with a TTL equal to their remaining lifetime. The overhead is one Redis read per request — acceptable for the security guarantee.

---

## Spring Cloud Gateway

**Why a dedicated API Gateway:**
- Single entry point simplifies CORS configuration.
- JWT validation is centralized — downstream services trust `X-User-Id` headers injected by the gateway.
- Rate limiting is applied before requests reach services.
- Route configuration is declarative YAML — no code changes needed to add new routes.

---

## React.js (Frontend)

**Why:**
- Component-based architecture maps perfectly to CineVerse's UI (MovieCard, ReviewForm, etc. are natural components).
- React Query handles server state (caching, background refetches, pagination) with minimal boilerplate.
- Zustand provides lightweight client state (auth) without Redux's ceremony.

---

## Docker + Docker Compose

**Why containers:**
- Eliminates "works on my machine" problems across the team.
- Each service runs in an isolated environment with exact dependencies.
- Multi-stage Dockerfiles keep images small (JRE-only runtime, no build tools).

**Why Compose over Kubernetes for dev:**
- Single `docker-compose up` command — zero Kubernetes knowledge required.
- In production, the same Docker images can run on Kubernetes with minimal changes.
