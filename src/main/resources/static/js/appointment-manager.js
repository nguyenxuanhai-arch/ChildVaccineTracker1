class AnimationUtils {
    static addTransition(element, className) {
        if (element) {
            element.classList.add(className);
            setTimeout(() => element.classList.remove(className), 500); // Adjust duration as needed
        }
    }
}


class AppointmentManager {
  constructor() {
    this.initializeComponents();
    this.attachEventListeners();
    this.currentStep = 1;
  }

  initializeComponents() {
    this.form = document.getElementById('appointmentForm');
    this.dateInput = document.getElementById('appointmentDate');
    this.timeInput = document.getElementById('appointmentTime');
    this.vaccineSelect = document.getElementById('vaccineType');
    this.stepButtons = document.querySelectorAll('.step-button');
    this.progressIndicator = document.getElementById('progressIndicator');
  }

  attachEventListeners() {
    if (this.form) {
      this.form.addEventListener('submit', this.handleSubmit.bind(this));
    }
    if (this.dateInput) {
      this.dateInput.addEventListener('change', this.validateDate.bind(this));
    }
    if (this.stepButtons) {
        this.stepButtons.forEach(button => {
            button.addEventListener('click', this.handleStepChange.bind(this));
        });
    }
  }

  validateDate(event) {
    const selected = new Date(event.target.value);
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    if (selected < today) {
      event.target.setCustomValidity('Ngày không được nhỏ hơn ngày hiện tại');
    } else {
      event.target.setCustomValidity('');
    }
  }

  async handleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(this.form);

    try {
      const response = await fetch('/api/appointments/create', {
        method: 'POST',
        body: formData,
        headers: {
          'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
      });

      if (!response.ok) {
        throw new Error('Lỗi khi đặt lịch hẹn');
      }

      this.showNotification('Đặt lịch hẹn thành công!', 'success');
      this.form.reset();
      this.showStep(1); //Reset to step 1 after submission
    } catch (error) {
      this.showNotification(error.message, 'error');
    }
  }

  showNotification(message, type) {
    const notification = document.createElement('div');
    notification.className = `alert alert-${type} alert-dismissible fade show`;
    notification.innerHTML = `
      ${message}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    const container = document.querySelector('.notification-container');
    if (container) {
      container.appendChild(notification);
      setTimeout(() => notification.remove(), 5000);
    }
  }

  showStep(stepNumber) {
    this.currentStep = stepNumber;
    this.updateProgress();

    const summarySection = document.querySelector('.summary-section');
    if (stepNumber === 4 && summarySection) {
        setTimeout(() => {
            summarySection.classList.add('visible');
        }, 100);
    } else if (summarySection) {
        summarySection.classList.remove('visible');
    }

    AnimationUtils.addTransition(
        document.querySelector('.tab-pane.active'),
        'step-transition'
    );
        // Update active tab and hide/show steps
    const steps = document.querySelectorAll('.tab-pane');
    steps.forEach((step, index) => {
        if (index + 1 === stepNumber) {
            step.classList.add('active');
        } else {
            step.classList.remove('active');
        }
    });

}

updateProgress() {
    const numSteps = this.stepButtons.length;
    const progress = (this.currentStep / numSteps) * 100;
    if (this.progressIndicator) {
        this.progressIndicator.style.width = progress + '%';
    }
}

handleStepChange(event) {
    const stepNumber = parseInt(event.target.dataset.step, 10);
    this.showStep(stepNumber);
}
}

document.addEventListener('DOMContentLoaded', () => {
  new AppointmentManager();
});