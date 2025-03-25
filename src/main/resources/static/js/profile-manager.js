
class ProfileManager {
    constructor() {
        this.bindEvents();
        this.loadUserProfile();
    }

    bindEvents() {
        const profileForm = document.getElementById('profile-form');
        if (profileForm) {
            profileForm.addEventListener('submit', (e) => this.handleProfileUpdate(e));
        }

        const passwordForm = document.getElementById('password-form');
        if (passwordForm) {
            passwordForm.addEventListener('submit', (e) => this.handlePasswordChange(e));
        }
    }

    async loadUserProfile() {
        try {
            const response = await fetch('/api/user/profile');
            if (response.ok) {
                const profile = await response.json();
                this.populateProfileForm(profile);
            }
        } catch (error) {
            console.error('Error loading profile:', error);
            this.showNotification('Error loading profile data', 'error');
        }
    }

    populateProfileForm(profile) {
        Object.keys(profile).forEach(key => {
            const input = document.getElementById(`profile-${key}`);
            if (input) {
                input.value = profile[key];
            }
        });
    }

    async handleProfileUpdate(event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const profileData = Object.fromEntries(formData.entries());

        try {
            const response = await fetch('/api/user/profile', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(profileData)
            });

            if (response.ok) {
                this.showNotification('Profile updated successfully', 'success');
            } else {
                throw new Error('Failed to update profile');
            }
        } catch (error) {
            console.error('Error updating profile:', error);
            this.showNotification('Error updating profile', 'error');
        }
    }

    async handlePasswordChange(event) {
        event.preventDefault();
        const formData = new FormData(event.target);
        const passwordData = Object.fromEntries(formData.entries());

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            this.showNotification('Passwords do not match', 'error');
            return;
        }

        try {
            const response = await fetch('/api/user/password', {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(passwordData)
            });

            if (response.ok) {
                this.showNotification('Password changed successfully', 'success');
                event.target.reset();
            } else {
                throw new Error('Failed to change password');
            }
        } catch (error) {
            console.error('Error changing password:', error);
            this.showNotification('Error changing password', 'error');
        }
    }

    showNotification(message, type) {
        const event = new CustomEvent('show-notification', {
            detail: { message, type }
        });
        document.dispatchEvent(event);
    }
}

// Initialize profile manager when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    new ProfileManager();
});
