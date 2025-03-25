class AnimationUtils {
    static addTransition(element, animation) {
        element.classList.add(animation);
        element.addEventListener('animationend', () => {
            element.classList.remove(animation);
        }, { once: true });
    }

    static fadeIn(element, duration = 300) {
        element.style.opacity = '0';
        element.style.display = 'block';
        element.style.transition = `opacity ${duration}ms ease-in`;
        requestAnimationFrame(() => {
            element.style.opacity = '1';
        });
    }

    static fadeOut(element, duration = 300) {
        element.style.opacity = '1';
        element.style.transition = `opacity ${duration}ms ease-out`;
        element.style.opacity = '0';
        setTimeout(() => {
            element.style.display = 'none';
        }, duration);
    }

    static slideIn(element, direction = 'left', duration = 300) {
        const start = direction === 'left' ? -100 : 100;
        element.style.transform = `translateX(${start}%)`;
        element.style.opacity = '0';
        element.style.display = 'block';
        element.style.transition = `transform ${duration}ms ease-out, opacity ${duration}ms ease-out`;
        requestAnimationFrame(() => {
            element.style.transform = 'translateX(0)';
            element.style.opacity = '1';
        });
    }

    static showLoading(container) {
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        container.appendChild(spinner);
        return spinner;
    }

    static hideLoading(spinner) {
        if (spinner && spinner.parentElement) {
            spinner.parentElement.removeChild(spinner);
        }
    }
}

// Export for global use
window.AnimationUtils = AnimationUtils;