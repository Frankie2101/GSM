document.addEventListener('DOMContentLoaded', function() {
    const userModal = new bootstrap.Modal(document.getElementById('userModal'));
    const searchBtn = document.getElementById('searchBtn');
    const addNewBtn = document.getElementById('addNewBtn');
    const saveUserBtn = document.getElementById('saveUserBtn');
    const userForm = document.getElementById('userForm');
    const userTableBody = document.getElementById('userTableBody');
    const keywordInput = document.getElementById('keyword');

    // --- FUNCTIONS ---

    // Function to fetch and render users
    async function fetchUsers() {
        const keyword = keywordInput.value;
        const response = await fetch(`/api/users?keyword=${encodeURIComponent(keyword)}`);
        const users = await response.json();

        userTableBody.innerHTML = ''; // Clear table
        if (users.length === 0) {
            userTableBody.innerHTML = '<tr><td colspan="8" class="text-center text-muted p-4">No users found.</td></tr>';
            return;
        }

        users.forEach((user, index) => {
            const statusBadge = user.activeFlag
                ? `<span class="badge bg-success">Active</span>`
                : `<span class="badge bg-danger">Inactive</span>`;

            // ==================== THAY ĐỔI 1: Sửa lại văn bản của nút ====================
            const actionButtons = `
                <a href="#" class="action-link me-3 edit-btn" data-id="${user.userId}">Edit</a>
                ${user.activeFlag
                ? `<a href="#" class="action-link text-danger deactivate-btn" data-id="${user.userId}">De-active</a>`
                : `<a href="#" class="action-link text-success activate-btn" data-id="${user.userId}">Active</a>`
            }
            `;

            // ==================== THAY ĐỔI 2: Thêm style để các nút không xuống dòng ====================
            const row = `
                <tr>
                    <td>${index + 1}</td>
                    <td>${user.userName}</td>
                    <td>${user.department || ''}</td>
                    <td>${user.productionLine || ''}</td>
                    <td>${user.phoneNumber}</td>
                    <td>${user.userType}</td>
                    <td>${statusBadge}</td>
                    <td class="align-middle" style="white-space: nowrap;">
                        ${actionButtons}
                    </td>
                </tr>
            `;
            userTableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // Function to open modal for editing
    async function openEditModal(userId) {
        const response = await fetch(`/api/users/${userId}`);
        const user = await response.json();

        document.getElementById('userModalLabel').textContent = 'Edit User';
        document.getElementById('userId').value = user.userId;
        document.getElementById('userName').value = user.userName;
        document.getElementById('phoneNumber').value = user.phoneNumber;
        document.getElementById('department').value = user.department || '';
        document.getElementById('productionLine').value = user.productionLine || '';
        document.getElementById('emailAddress').value = user.emailAddress || '';
        document.getElementById('userType').value = user.userType;

        userModal.show();
    }

    // Function to save user (Create or Update)
    async function saveUser() {
        // ... (phần logic save không thay đổi)
        const userData = {
            userId: document.getElementById('userId').value || null,
            userName: document.getElementById('userName').value,
            phoneNumber: document.getElementById('phoneNumber').value,
            department: document.getElementById('department').value,
            productionLine: document.getElementById('productionLine').value,
            emailAddress: document.getElementById('emailAddress').value,
            userType: document.getElementById('userType').value
        };

        const response = await fetch('/api/users', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(userData)
        });

        if (response.ok) {
            userModal.hide();
            fetchUsers(); // Refresh table
            Swal.fire('Success', 'User saved successfully!', 'success');
        } else {
            const errorData = await response.json();
            Swal.fire('Error', errorData.message || 'Failed to save user.', 'error');
        }
    }

    // --- EVENT LISTENERS ---

    searchBtn.addEventListener('click', fetchUsers);
    keywordInput.addEventListener('keypress', (e) => {
        if (e.key === 'Enter') fetchUsers();
    });

    addNewBtn.addEventListener('click', () => {
        document.getElementById('userModalLabel').textContent = 'Add New User';
        userForm.reset();
        document.getElementById('userId').value = '';
        userModal.show();
    });

    saveUserBtn.addEventListener('click', saveUser);

    userTableBody.addEventListener('click', async (e) => {
        e.preventDefault();
        const target = e.target;
        const userId = target.dataset.id;

        if (!userId) return;

        if (target.classList.contains('edit-btn')) {
            openEditModal(userId);
        } else if (target.classList.contains('deactivate-btn')) {
            const result = await Swal.fire({
                title: 'Are you sure?',
                text: "Do you want to de-active this user?",
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#3085d6',
                confirmButtonText: 'Yes, de-active it!'
            });
            if (result.isConfirmed) {
                await fetch(`/api/users/${userId}/deactivate`, { method: 'POST' });
                fetchUsers();
            }
        } else if (target.classList.contains('activate-btn')) {
            await fetch(`/api/users/${userId}/activate`, { method: 'POST' });
            fetchUsers();
        }
    });

    // Initial load
    fetchUsers();
});