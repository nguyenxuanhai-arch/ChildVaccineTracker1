// Utility Functions
const utils = {
  formatDate: (date) => {
    return new Date(date).toLocaleDateString('vi-VN');
  },

  formatCurrency: (amount) => {
    return new Intl.NumberFormat('vi-VN', { 
      style: 'currency', 
      currency: 'VND' 
    }).format(amount);
  }
};

// Form Validation
const validateForm = (formElement) => {
  const inputs = formElement.querySelectorAll('input[required], select[required]');
  let isValid = true;

  inputs.forEach(input => {
    if (!input.value.trim()) {
      input.classList.add('is-invalid');
      isValid = false;
    } else {
      input.classList.remove('is-invalid');
    }
  });

  return isValid;
};

// Toast Notifications
const showToast = (message, type = 'info') => {
  const toastContainer = document.getElementById('toast-container') 
    || createToastContainer();

  const toast = document.createElement('div');
  toast.className = `toast toast-${type} show`;
  toast.innerHTML = message;

  toastContainer.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
};

const createToastContainer = () => {
  const container = document.createElement('div');
  container.id = 'toast-container';
  document.body.appendChild(container);
  return container;
};

// Sidebar Toggle
const toggleSidebar = () => {
  const sidebar = document.querySelector('.sidebar');
  const content = document.querySelector('#content-wrapper');

  if (sidebar && content) {
    sidebar.classList.toggle('collapsed');
    content.classList.toggle('expanded');
  }
};

// Data Table Initialization
// Utilities
const formatDate = (date) => {
  return new Intl.DateTimeFormat('vi-VN').format(new Date(date));
};

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount);
};

//DataTable initialization
const initDataTable = (tableId, options = {}) => {
  const table = document.getElementById(tableId);
  if (!table) return;

  const defaultOptions = {
    pageLength: 10,
    language: {
      url: '//cdn.datatables.net/plug-ins/1.10.24/i18n/Vietnamese.json'
    }
  };

  return new DataTable(table, { ...defaultOptions, ...options });
};


// Main JavaScript functionality
class NotificationManager {
    constructor() {
        this.container = document.querySelector('.notification-container');
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.className = 'notification-container';
            document.body.appendChild(this.container);
        }
    }

    show(message, type = 'info', duration = 5000) {
        const notification = document.createElement('div');
        notification.className = `alert alert-${type} alert-dismissible fade show`;
        notification.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        `;

        this.container.appendChild(notification);
        setTimeout(() => notification.remove(), duration);
    }
}

class ProfileManager {
    constructor() {
        this.initializeProfile();
        this.attachEventListeners();
    }

    initializeProfile() {
        this.profileForm = document.getElementById('profileForm');
        this.avatarUpload = document.getElementById('avatarUpload');
    }

    attachEventListeners() {
        if (this.profileForm) {
            this.profileForm.addEventListener('submit', this.handleProfileUpdate.bind(this));
        }
        if (this.avatarUpload) {
            this.avatarUpload.addEventListener('change', this.handleAvatarUpload.bind(this));
        }
    }

    async handleProfileUpdate(event) {
        event.preventDefault();
        const formData = new FormData(this.profileForm);

        try {
            const response = await fetch('/api/profile/update', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            });

            if (!response.ok) throw new Error('Cập nhật thất bại');

            notificationManager.show('Cập nhật thông tin thành công!', 'success');
        } catch (error) {
            notificationManager.show(error.message, 'danger');
        }
    }

    async handleAvatarUpload(event) {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append('avatar', file);

        try {
            const response = await fetch('/api/profile/avatar', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            });

            if (!response.ok) throw new Error('Tải ảnh thất bại');

            notificationManager.show('Cập nhật ảnh đại diện thành công!', 'success');
        } catch (error) {
            notificationManager.show(error.message, 'danger');
        }
    }
}

// Initialize managers
const notificationManager = new NotificationManager();
const profileManager = new ProfileManager();

// Global event handlers
document.addEventListener('DOMContentLoaded', () => {
    // Initialize Bootstrap tooltips
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl));

    // Initialize Bootstrap popovers
    const popoverTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="popover"]'));
    popoverTriggerList.map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl));

  // Form validation
  const forms = document.querySelectorAll('form');
  forms.forEach(form => {
    form.addEventListener('submit', (e) => {
      if (!validateForm(form)) {
        e.preventDefault();
        showToast('Vui lòng điền đầy đủ thông tin', 'error');
      }
    });
  });

  // Sidebar toggle button
  const sidebarToggle = document.querySelector('.sidebar-toggle');
  if (sidebarToggle) {
    sidebarToggle.addEventListener('click', toggleSidebar);
  }

    // Initialize all components
    initializePatientRecords();
});


function initializePatientRecords() {
    const patientCards = document.querySelectorAll('.patient-record');

    patientCards.forEach(card => {
        card.addEventListener('click', function(e) {
            if (e.target.classList.contains('view-history-btn')) {
                const patientId = this.dataset.patientId;
                loadVaccinationHistory(patientId);
            }
        });
    });
}

function loadVaccinationHistory(patientId) {
    fetch(`/api/patients/${patientId}/vaccination-history`)
        .then(response => response.json())
        .then(data => {
            updateHistoryDisplay(data);
        })
        .catch(error => {
            console.error('Error loading vaccination history:', error);
            showNotification('Error loading vaccination history', 'danger');
        });
}

function showNotification(message, type = 'info') {
    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show`;
    alertDiv.setAttribute('role', 'alert');
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    const container = document.querySelector('.container-fluid');
    container.insertBefore(alertDiv, container.firstChild);

    setTimeout(() => {
        alertDiv.remove();
    }, 5000);
}

// Export functions for global use
window.utils = utils;
window.showToast = showToast;
window.initDataTable = initDataTable;