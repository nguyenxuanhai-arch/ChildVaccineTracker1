class SettingsMenu {
    constructor() {
        this.init();
        this.bindEvents();
    }

    init() {
        this.menu = document.querySelector('.settings-menu');
        this.overlay = document.querySelector('.settings-overlay');
    }

    bindEvents() {
        const toggleBtn = document.querySelector('.settings-toggle');
        if (toggleBtn) {
            toggleBtn.addEventListener('click', () => this.toggleMenu());
        }

        document.addEventListener('click', (e) => {
            if (this.menu && !this.menu.contains(e.target) && 
                e.target.className !== 'settings-toggle') {
                this.closeMenu();
            }
        });
    }

    toggleMenu() {
        if (this.menu) {
            this.menu.classList.toggle('active');
            if (this.overlay) {
                this.overlay.classList.toggle('active');
            }
        }
    }

    closeMenu() {
        if (this.menu) {
            this.menu.classList.remove('active');
            if (this.overlay) {
                this.overlay.classList.remove('active');
            }
        }
    }
}

document.addEventListener('DOMContentLoaded', () => {
    new SettingsMenu();
});