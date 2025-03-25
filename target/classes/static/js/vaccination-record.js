
// Vaccination Record Management
class VaccinationRecordManager {
    constructor() {
        this.initializeElements();
        this.attachEventListeners();
    }

    initializeElements() {
        this.recordForm = document.getElementById('vaccinationRecordForm');
        this.recordTable = document.getElementById('vaccinationRecordTable');
        this.searchInput = document.getElementById('searchRecord');
    }

    attachEventListeners() {
        if (this.recordForm) {
            this.recordForm.addEventListener('submit', this.handleRecordSubmit.bind(this));
        }
        
        if (this.searchInput) {
            this.searchInput.addEventListener('input', this.handleSearch.bind(this));
        }
    }

    async handleRecordSubmit(event) {
        event.preventDefault();
        const formData = new FormData(this.recordForm);

        try {
            const response = await fetch('/api/vaccination-records/add', {
                method: 'POST',
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('meta[name="_csrf"]').content
                }
            });

            if (!response.ok) throw new Error('Thêm hồ sơ thất bại');

            notificationManager.show('Thêm hồ sơ tiêm chủng thành công!', 'success');
            this.recordForm.reset();
            this.refreshRecordTable();
        } catch (error) {
            notificationManager.show(error.message, 'danger');
        }
    }

    async handleSearch(event) {
        const searchTerm = event.target.value.toLowerCase();
        const rows = this.recordTable.querySelectorAll('tbody tr');

        rows.forEach(row => {
            const text = row.textContent.toLowerCase();
            row.style.display = text.includes(searchTerm) ? '' : 'none';
        });
    }

    async refreshRecordTable() {
        try {
            const response = await fetch('/api/vaccination-records');
            const records = await response.json();
            
            const tbody = this.recordTable.querySelector('tbody');
            tbody.innerHTML = records.map(record => `
                <tr>
                    <td>${record.patientName}</td>
                    <td>${record.vaccineName}</td>
                    <td>${new Date(record.dateAdministered).toLocaleDateString('vi-VN')}</td>
                    <td>${record.dosageNumber}</td>
                    <td>
                        <button class="btn btn-sm btn-info" onclick="viewRecord(${record.id})">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="btn btn-sm btn-warning" onclick="editRecord(${record.id})">
                            <i class="fas fa-edit"></i>
                        </button>
                    </td>
                </tr>
            `).join('');
        } catch (error) {
            notificationManager.show('Không thể tải dữ liệu hồ sơ', 'danger');
        }
    }
}

// Initialize manager
const vaccinationRecordManager = new VaccinationRecordManager();
