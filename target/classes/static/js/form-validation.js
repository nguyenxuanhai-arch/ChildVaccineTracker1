class FormValidator {
    constructor(formId, options = {}) {
        this.form = document.getElementById(formId);
        this.options = {
            errorClass: 'is-invalid',
            successClass: 'is-valid',
            ...options
        };
        this.setupValidation();
    }

    setupValidation() {
        if (!this.form) return;
        
        this.form.addEventListener('submit', (e) => {
            if (!this.validateForm()) {
                e.preventDefault();
                e.stopPropagation();
            }
            this.form.classList.add('was-validated');
        });

        // Real-time validation
        this.form.querySelectorAll('input, select, textarea').forEach(input => {
            input.addEventListener('input', () => this.validateField(input));
            input.addEventListener('blur', () => this.validateField(input));
        });
    }

    validateField(field) {
        const value = field.value.trim();
        let isValid = true;
        let errorMessage = '';

        // Required field validation
        if (field.hasAttribute('required') && !value) {
            isValid = false;
            errorMessage = 'This field is required';
        }

        // Email validation
        if (field.type === 'email' && value) {
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            if (!emailRegex.test(value)) {
                isValid = false;
                errorMessage = 'Please enter a valid email address';
            }
        }

        // Phone validation
        if (field.dataset.type === 'phone' && value) {
            const phoneRegex = /^\+?[\d\s-]{10,}$/;
            if (!phoneRegex.test(value)) {
                isValid = false;
                errorMessage = 'Please enter a valid phone number';
            }
        }

        this.updateFieldStatus(field, isValid, errorMessage);
        return isValid;
    }

    validateForm() {
        let isValid = true;
        this.form.querySelectorAll('input, select, textarea').forEach(field => {
            if (!this.validateField(field)) {
                isValid = false;
            }
        });
        return isValid;
    }

    updateFieldStatus(field, isValid, errorMessage = '') {
        field.classList.remove(this.options.errorClass, this.options.successClass);
        field.classList.add(isValid ? this.options.successClass : this.options.errorClass);
        
        let feedback = field.nextElementSibling;
        if (!feedback || !feedback.classList.contains('invalid-feedback')) {
            feedback = document.createElement('div');
            feedback.className = 'invalid-feedback';
            field.parentNode.insertBefore(feedback, field.nextSibling);
        }
        feedback.textContent = errorMessage;
    }

    reset() {
        this.form.reset();
        this.form.classList.remove('was-validated');
        this.form.querySelectorAll('input, select, textarea').forEach(field => {
            field.classList.remove(this.options.errorClass, this.options.successClass);
        });
    }
}

// Global form validation initialization
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('form[data-validate="true"]').forEach(form => {
        new FormValidator(form.id);
    });
});