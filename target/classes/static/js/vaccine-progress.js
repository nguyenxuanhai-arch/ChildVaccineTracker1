
// Vaccine Progress Tracking
class VaccineProgressTracker {
  constructor() {
    this.progressBar = document.querySelector('.vaccination-progress');
    this.initializeProgress();
  }

  initializeProgress() {
    if (!this.progressBar) return;
    
    const completedVaccines = parseInt(this.progressBar.dataset.completed) || 0;
    const totalVaccines = parseInt(this.progressBar.dataset.total) || 0;
    
    if (totalVaccines > 0) {
      const percentage = (completedVaccines / totalVaccines) * 100;
      this.updateProgress(percentage);
    }
  }

  updateProgress(percentage) {
    const progressElement = this.progressBar.querySelector('.progress-bar');
    if (progressElement) {
      progressElement.style.width = `${percentage}%`;
      progressElement.setAttribute('aria-valuenow', percentage);
      
      // Add animation class
      progressElement.classList.add('progress-bar-animated');
      
      // Update status label
      if (percentage === 100) {
        this.showCompletionMessage();
      }
    }
  }

  showCompletionMessage() {
    const messageContainer = document.createElement('div');
    messageContainer.className = 'alert alert-success mt-3';
    messageContainer.innerHTML = `
      <i class="fas fa-check-circle me-2"></i>
      Chúc mừng! Bé đã hoàn thành tất cả các mũi tiêm theo lịch
    `;
    this.progressBar.parentNode.appendChild(messageContainer);
  }
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', () => {
  new VaccineProgressTracker();
});
