# Local Development Guide

## Prerequisites

| Tool | Version | Install |
|------|---------|---------|
| Docker Desktop | 24+ | https://docker.com |
| JDK | 17+ | https://adoptium.net |
| Node.js | 20+ | https://nodejs.org |
| Maven | 3.9+ | https://maven.apache.org |

---

## Option A: Full Docker Stack (Recommended)

One command starts everything:

```bash
cd cineverse
docker-compose up --build
```

Wait for all services to show as healthy (~90 seconds on first run).

Verify:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
```

---

## Option B: Run Services Locally (Faster iteration)

### 1. Start infrastructure only

```bash
docker-compose up postgres-auth postgres-movies mongodb redis rabbitmq -d
```

### 2. Run each service

Open 4 terminals:

```bash
# Terminal 1 — Auth Service
cd auth-service
./mvnw spring-boot:run

# Terminal 2 — Movie Service
cd movie-service
./mvnw spring-boot:run

# Terminal 3 — Review Service
cd review-service
./mvnw spring-boot:run

# Terminal 4 — API Gateway
cd api-gateway
./mvnw spring-boot:run
```

### 3. Run frontend

```bash
cd frontend
npm install
npm start
```

Frontend opens at http://localhost:3000

---

## Environment Variables

Each service reads from `application.yml`. For local dev, defaults work as-is.
For production, override via environment variables (see docker-compose.yml).

Key secrets to change before going to production:
- `JWT_SECRET` — use a cryptographically random 256-bit value
- All database passwords
- Redis password

---

## Database Access

```bash
# PostgreSQL — Auth
docker exec -it cineverse-postgres-auth psql -U cineverse -d auth_db

# PostgreSQL — Movies
docker exec -it cineverse-postgres-movies psql -U cineverse -d movies_db

# MongoDB
docker exec -it cineverse-mongodb mongosh -u cineverse -p cineverse_pass --authenticationDatabase admin

# Redis CLI
docker exec -it cineverse-redis redis-cli -a cineverse_redis
```

---

## RabbitMQ Management UI

http://localhost:15672 — login: `cineverse` / `cineverse_pass`

You can see queues, exchanges, and message flow here.

---

## Testing the API

Register a user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H 'Content-Type: application/json' \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123",
    "displayName": "Test User"
  }'
```

Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"identifier": "testuser", "password": "Test@123"}'
```

Search movies (public):
```bash
curl "http://localhost:8080/api/v1/movies/search?q=inception"
```

Submit a review (replace TOKEN):
```bash
curl -X POST http://localhost:8080/api/v1/reviews \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer TOKEN' \
  -d '{
    "movieId": "MOVIE_ID",
    "rating": 9,
    "title": "Amazing film",
    "content": "This movie completely blew my mind.",
    "containsSpoilers": false
  }'
```
