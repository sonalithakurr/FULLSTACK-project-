# CineVerse 🎬

A production-ready, distributed microservices-based movie discovery and management platform inspired by Netflix/IMDb.

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                         CLIENT LAYER                            │
│                    React.js Frontend (3000)                     │
└─────────────────────────┬───────────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────────┐
│                       API GATEWAY (8080)                        │
│              JWT Validation · Rate Limiting · Routing           │
└──────┬──────────────────┬──────────────────────┬───────────────┘
       │                  │                      │
┌──────▼──────┐  ┌────────▼────────┐  ┌─────────▼──────────┐
│ Auth Service│  │  Movie Service  │  │  Review Service    │
│   (8081)    │  │    (8082)       │  │     (8083)         │
└──────┬──────┘  └────────┬────────┘  └─────────┬──────────┘
       │                  │                      │
┌──────▼──────┐  ┌────────▼────────┐  ┌─────────▼──────────┐
│ PostgreSQL  │  │   PostgreSQL    │  │      MongoDB       │
│  (users)    │  │   (movies)      │  │    (reviews)       │
└─────────────┘  └────────┬────────┘  └────────────────────┘
                          │
                  ┌───────▼───────┐
                  │     Redis     │
                  │   (cache)     │
                  └───────────────┘
                          │
                  ┌───────▼───────┐
                  │   RabbitMQ    │
                  │  (messaging)  │
                  └───────────────┘
```

## Technology Stack

| Layer | Technology | Reason |
|-------|-----------|--------|
| Frontend | React.js | Component-based, fast rendering |
| API Gateway | Spring Cloud Gateway | Centralized routing, auth |
| Auth Service | Spring Boot + JWT | Stateless, scalable auth |
| Movie Service | Spring Boot | REST API + caching |
| Review Service | Spring Boot | Async messaging |
| Primary DB | PostgreSQL | ACID, relational data |
| Document DB | MongoDB | Flexible schema for reviews |
| Cache | Redis | Sub-ms latency, TTL support |
| Message Broker | RabbitMQ | Async event-driven comms |
| Containerization | Docker + Compose | One-command startup |
| CI/CD | GitHub Actions | Automated pipelines |

## Quick Start

```bash
# Clone and start everything
git clone https://github.com/yourorg/cineverse.git
cd cineverse
docker-compose up --build
```

Services will be available at:
- Frontend:     http://localhost:3000
- API Gateway:  http://localhost:8080
- Auth Service: http://localhost:8081
- Movie Service:http://localhost:8082
- Review Service:http://localhost:8083
- RabbitMQ UI: http://localhost:15672 (guest/guest)

## Project Structure

```
cineverse/
├── api-gateway/          # Spring Cloud Gateway
├── auth-service/         # JWT authentication
├── movie-service/        # Movie discovery & management
├── review-service/       # Reviews & ratings
├── frontend/             # React.js application
├── database/             # SQL schemas & MongoDB examples
├── docs/                 # Architecture & API docs
├── .github/workflows/    # CI/CD pipelines
└── docker-compose.yml    # Full stack orchestration
```

## Development Setup

See [docs/LOCAL_DEVELOPMENT.md](docs/LOCAL_DEVELOPMENT.md) for detailed instructions.

## API Documentation

See [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md) for full API reference.
