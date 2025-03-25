
// Chart Initialization Functions
const initRevenueChart = (labels, data) => {
  const ctx = document.getElementById('revenueChart').getContext('2d');
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: 'Doanh thu',
        data: data,
        borderColor: '#0d6efd',
        tension: 0.1,
        fill: false
      }]
    },
    options: {
      responsive: true,
      scales: {
        y: {
          beginAtZero: true,
          ticks: {
            callback: (value) => utils.formatCurrency(value)
          }
        }
      }
    }
  });
};

const initPaymentMethodsChart = (labels, data) => {
  const ctx = document.getElementById('paymentChart').getContext('2d');
  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: labels,
      datasets: [{
        data: data,
        backgroundColor: [
          '#0d6efd',
          '#198754',
          '#ffc107',
          '#dc3545'
        ]
      }]
    },
    options: {
      responsive: true,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    }
  });
};

// Export functions
window.initRevenueChart = initRevenueChart;
window.initPaymentMethodsChart = initPaymentMethodsChart;
