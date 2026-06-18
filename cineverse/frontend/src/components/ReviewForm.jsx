import React, { useState } from 'react';
import { Star } from 'lucide-react';
import { reviewsApi } from '../api/reviews';
import toast from 'react-hot-toast';

/**
 * ReviewForm — create or edit a review.
 *
 * Props:
 *  - movieId: string
 *  - existingReview: Review | null (if editing)
 *  - onSuccess: callback(review)
 */
function ReviewForm({ movieId, existingReview = null, onSuccess }) {
    const [rating, setRating] = useState(existingReview?.rating || 0);
    const [hoveredRating, setHoveredRating] = useState(0);
    const [title, setTitle] = useState(existingReview?.title || '');
    const [content, setContent] = useState(existingReview?.content || '');
    const [containsSpoilers, setContainsSpoilers] = useState(
        existingReview?.containsSpoilers || false
    );
    const [submitting, setSubmitting] = useState(false);

    const isEditing = !!existingReview;

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (rating === 0) {
            toast.error('Please select a rating');
            return;
        }

        setSubmitting(true);
        try {
            const payload = { movieId, rating, title, content, containsSpoilers };
            let result;
            if (isEditing) {
                result = await reviewsApi.updateReview(existingReview.id, payload);
            } else {
                result = await reviewsApi.createReview(payload);
            }
            toast.success(isEditing ? 'Review updated!' : 'Review submitted!');
            onSuccess?.(result.data);
        } catch (err) {
            const msg = err.response?.data?.detail || 'Failed to submit review';
            toast.error(msg);
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <form
            onSubmit={handleSubmit}
            className="review-form"
            aria-label={isEditing ? 'Edit review' : 'Write a review'}
        >
            <h3 className="review-form__heading">
                {isEditing ? 'Edit Your Review' : 'Write a Review'}
            </h3>

            {/* Star Rating */}
            <fieldset className="review-form__rating-group">
                <legend className="form-label">Your Rating *</legend>
                <div
                    className="star-rating"
                    role="radiogroup"
                    aria-required="true"
                >
                    {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((star) => (
                        <button
                            key={star}
                            type="button"
                            className={`star-rating__star ${
                                star <= (hoveredRating || rating) ? 'active' : ''
                            }`}
                            onClick={() => setRating(star)}
                            onMouseEnter={() => setHoveredRating(star)}
                            onMouseLeave={() => setHoveredRating(0)}
                            aria-label={`Rate ${star} out of 10`}
                            aria-pressed={rating === star}
                        >
                            <Star size={20} aria-hidden="true" />
                        </button>
                    ))}
                    <span className="star-rating__value" aria-live="polite">
                        {rating > 0 ? `${rating}/10` : 'Select rating'}
                    </span>
                </div>
            </fieldset>

            {/* Title */}
            <div className="form-group">
                <label htmlFor="review-title" className="form-label">
                    Review Title *
                </label>
                <input
                    id="review-title"
                    type="text"
                    className="form-input"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="Summarize your thoughts"
                    maxLength={150}
                    required
                    aria-required="true"
                />
            </div>

            {/* Content */}
            <div className="form-group">
                <label htmlFor="review-content" className="form-label">
                    Your Review *
                </label>
                <textarea
                    id="review-content"
                    className="form-input form-textarea"
                    value={content}
                    onChange={(e) => setContent(e.target.value)}
                    placeholder="Share what you thought about this film..."
                    rows={5}
                    minLength={10}
                    maxLength={5000}
                    required
                    aria-required="true"
                />
                <span className="form-hint" aria-live="polite">
                    {content.length}/5000 characters
                </span>
            </div>

            {/* Spoiler toggle */}
            <div className="form-group form-group--inline">
                <input
                    id="contains-spoilers"
                    type="checkbox"
                    checked={containsSpoilers}
                    onChange={(e) => setContainsSpoilers(e.target.checked)}
                    aria-describedby="spoiler-hint"
                />
                <label htmlFor="contains-spoilers" className="form-label form-label--inline">
                    Contains spoilers
                </label>
                <span id="spoiler-hint" className="form-hint">
                    Check this if your review reveals plot details
                </span>
            </div>

            <button
                type="submit"
                className="btn btn--primary"
                disabled={submitting || rating === 0 || !title.trim() || content.length < 10}
                aria-busy={submitting}
            >
                {submitting
                    ? 'Submitting...'
                    : isEditing ? 'Update Review' : 'Submit Review'}
            </button>
        </form>
    );
}

export default ReviewForm;
