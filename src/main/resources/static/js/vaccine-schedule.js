
// Vaccine Schedule Management
class VaccineScheduleManager {
  constructor() {
    this.initializeEventListeners();
  }

  initializeEventListeners() {
    const scheduleForm = document.getElementById('scheduleForm');
    if (scheduleForm) {
      scheduleForm.addEventListener('submit', this.handleScheduleSubmit.bind(this));
    }

    const dateInputs = document.querySelectorAll('input[type="date"]');
    dateInputs.forEach(input => {
      input.addEventListener('change', this.validateDate.bind(this));
    });
  }

  validateDate(event) {
    const selectedDate = new Date(event.target.value);
    const today = new Date();
    
    if (selectedDate < today) {
      event.target.setCustomValidity('Ngày không được nhỏ hơn ngày hiện tại');
    } else {
      event.target.setCustomValidity('');
    }
  }

  async handleScheduleSubmit(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    
    try {
      const response = await fetch('/api/vaccine-schedule/create', {
        method: 'POST',
        body: formData,
        headers: {
          'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
        }
      });

      if (!response.ok) throw new Error('Lỗi khi đặt lịch');
      
      showToast('Đặt lịch thành công', 'success');
      event.target.reset();
    } catch (error) {
      showToast(error.message, 'error');
    }
  }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
  new VaccineScheduleManager();
});
