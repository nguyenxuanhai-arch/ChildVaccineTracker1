
class FeedbackSystem {
    constructor() {
        this.bindEvents();
        this.loadExistingFeedback();
    }

    bindEvents() {
        const feedbackForm = document.getElementById('feedback-form');
        if (feedbackForm) {
            feedbackForm.addEventListener('submit', (e) => this.handleFeedbackSubmission(e));
        }

        const ratingStars = document.querySelectorAll('.rating-star');
        ratingStars.forEach(star => {
            star.addEventListener('click', (e) => this.handleStarRating(e));
        });
    }

    async handleFeedbackSubmission(event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const feedbackData = {
            rating: formData.get('rating'),
            comment: formData.get('comment'),
            serviceId: formData.get('serviceId')
        };

        try {
            const response = await fetch('/api/feedback/submit', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(feedbackData)
            });

            if (response.ok) {
                this.showNotification('Feedback submitted successfully', 'success');
                this.loadExistingFeedback();
            } else {
                throw new Error('Failed to submit feedback');
            }
        } catch (error) {
            console.error('Error submitting feedback:', error);
            this.showNotification('Error submitting feedback', 'error');
        }
    }

    handleStarRating(event) {
        const rating = event.target.dataset.rating;
        const stars = document.querySelectorAll('.rating-star');
        
        stars.forEach(star => {
            const starRating = star.dataset.rating;
            star.classList.toggle('active', starRating <= rating);
        });
        
        document.getElementById('rating-input').value = rating;
    }

    async loadExistingFeedback() {
        try {
            const serviceId = document.getElementById('service-id').value;
            const response = await fetch(`/api/feedback/${serviceId}`);
            if (response.ok) {
                const feedback = await response.json();
                this.displayFeedback(feedback);
            }
        } catch (error) {
            console.error('Error loading feedback:', error);
        }
    }

    displayFeedback(feedbackList) {
        const container = document.getElementById('feedback-list');
        if (!container) return;

        container.innerHTML = feedbackList.map(feedback => `
            <div class="feedback-item">
                <div class="rating">
                    ${this.generateStars(feedback.rating)}
                </div>
                <p class="comment">${feedback.comment}</p>
                <small class="date">${new Date(feedback.createdAt).toLocaleDateString()}</small>
            </div>
        `).join('');
    }

    generateStars(rating) {
        return Array(5).fill(0).map((_, index) => 
            `<span class="star ${index < rating ? 'filled' : ''}">★</span>`
        ).join('');
    }

    showNotification(message, type) {
        const event = new CustomEvent('show-notification', {
            detail: { message, type }
        });
        document.dispatchEvent(event);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new FeedbackSystem();
});
