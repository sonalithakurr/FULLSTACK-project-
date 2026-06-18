// ============================================================
// CineVerse Review Service — MongoDB Initialization
// ============================================================
// This script runs once when the container starts.
// It creates indexes and seeds example data.
// ============================================================

db = db.getSiblingDB('reviews_db');

// ─── Indexes ──────────────────────────────────────────────────────────────────

// Primary query pattern: reviews by movie, sorted by date
db.reviews.createIndex({ movieId: 1, createdAt: -1 }, { name: "idx_movie_reviews" });

// Enforce one review per user per movie
db.reviews.createIndex(
    { userId: 1, movieId: 1 },
    { unique: true, name: "idx_user_movie_unique" }
);

// User's own reviews
db.reviews.createIndex({ userId: 1 }, { name: "idx_user_reviews" });

// Text search on review content
db.reviews.createIndex(
    { title: "text", content: "text" },
    { name: "idx_reviews_fts", weights: { title: 3, content: 1 } }
);

// ─── Example Documents ────────────────────────────────────────────────────────

db.reviews.insertMany([
    {
        movieId: "inception-placeholder-id",
        userId: "user-placeholder-id",
        username: "cinephile99",
        userDisplayName: "Alex M.",
        rating: 9,
        title: "A mind-bending masterpiece",
        content: "Nolan outdoes himself with this layered, intricate thriller that rewards repeated viewing. The concept of entering dreams is executed with stunning precision. The ending leaves you questioning reality.",
        tags: ["mind-bending", "great-cinematography", "must-rewatch"],
        containsSpoilers: false,
        helpfulCount: 42,
        helpfulVoterIds: [],
        createdAt: new Date("2024-01-15T10:30:00Z"),
        updatedAt: new Date("2024-01-15T10:30:00Z")
    },
    {
        movieId: "inception-placeholder-id",
        userId: "user-placeholder-id-2",
        username: "movielover",
        userDisplayName: "Jamie K.",
        rating: 8,
        title: "Complex but rewarding",
        content: "Takes multiple viewings to fully grasp, but every frame has a purpose. Hans Zimmer's score is iconic. DiCaprio delivers one of his best performances.",
        tags: ["complex", "great-score", "great-acting"],
        containsSpoilers: true,
        helpfulCount: 28,
        helpfulVoterIds: [],
        createdAt: new Date("2024-01-20T14:15:00Z"),
        updatedAt: new Date("2024-01-20T14:15:00Z")
    }
]);

print("MongoDB reviews_db initialized successfully.");
