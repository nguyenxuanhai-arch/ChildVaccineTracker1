
class AppointmentManager {
  constructor() {
    this.initializeComponents();
    this.attachEventListeners();
  }

  initializeComponents() {
    this.form = document.getElementById('appointmentForm');
    this.dateInput = document.getElementById('appointmentDate');
    this.timeInput = document.getElementById('appointmentTime');
    this.vaccineSelect = document.getElementById('vaccineType');
  }

  attachEventListeners() {
    if (this.form) {
      this.form.addEventListener('submit', this.handleSubmit.bind(this));
    }
    if (this.dateInput) {
      this.dateInput.addEventListener('change', this.validateDate.bind(this));
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
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new AppointmentManager();
});
