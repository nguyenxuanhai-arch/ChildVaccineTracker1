
class UserPreferences {
    constructor() {
        this.bindEvents();
        this.loadPreferences();
    }

    bindEvents() {
        const notificationToggle = document.getElementById('notification-toggle');
        if (notificationToggle) {
            notificationToggle.addEventListener('change', () => this.updatePreference('notifications'));
        }

        const languageSelect = document.getElementById('language-select');
        if (languageSelect) {
            languageSelect.addEventListener('change', (e) => this.updatePreference('language', e.target.value));
        }

        const themeToggle = document.getElementById('theme-toggle');
        if (themeToggle) {
            themeToggle.addEventListener('change', () => this.updatePreference('darkMode'));
        }
    }

    async loadPreferences() {
        try {
            const response = await fetch('/api/user/preferences');
            if (response.ok) {
                const preferences = await response.json();
                this.applyPreferences(preferences);
            }
        } catch (error) {
            console.error('Error loading preferences:', error);
        }
    }

    applyPreferences(preferences) {
        if (preferences.darkMode) {
            document.body.classList.add('dark-mode');
        }
        
        const notificationToggle = document.getElementById('notification-toggle');
        if (notificationToggle) {
            notificationToggle.checked = preferences.notifications;
        }

        const languageSelect = document.getElementById('language-select');
        if (languageSelect) {
            languageSelect.value = preferences.language;
        }
    }

    async updatePreference(key, value) {
        try {
            const response = await fetch('/api/user/preferences', {
                method: 'PATCH',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    [key]: value !== undefined ? value : document.getElementById(`${key}-toggle`).checked
                })
            });

            if (response.ok) {
                this.showNotification('Preferences updated successfully', 'success');
            } else {
                throw new Error('Failed to update preferences');
            }
        } catch (error) {
            console.error('Error updating preferences:', error);
            this.showNotification('Error updating preferences', 'error');
        }
    }

    showNotification(message, type) {
        const event = new CustomEvent('show-notification', {
            detail: { message, type }
        });
        document.dispatchEvent(event);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new UserPreferences();
});
