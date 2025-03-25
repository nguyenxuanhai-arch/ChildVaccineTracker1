
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

// Event Listeners
document.addEventListener('DOMContentLoaded', () => {
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

  // Initialize tooltips
  const tooltipTriggerList = [].slice.call(
    document.querySelectorAll('[data-bs-toggle="tooltip"]')
  );
  tooltipTriggerList.map(tooltipTriggerEl => 
    new bootstrap.Tooltip(tooltipTriggerEl)
  );
});

// Export functions for global use
window.utils = utils;
window.showToast = showToast;
window.initDataTable = initDataTable;
