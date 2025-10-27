document.addEventListener('DOMContentLoaded', function() {
    const userTypeSelect = document.getElementById('userType');
    const permissionSection = document.getElementById('permissionSection');
    const userModal = new bootstrap.Modal(document.getElementById('userModal'));
    const searchBtn = document.getElementById('searchBtn');
    const addNewBtn = document.getElementById('addNewBtn');
    const saveUserBtn = document.getElementById('saveUserBtn');
    const userForm = document.getElementById('userForm');
    const userTableBody = document.getElementById('userTableBody');
    const keywordInput = document.getElementById('keyword');

    // --- FUNCTIONS ---

    /**
    *Function to show/hide the permissions block based on the selected User Type
    */
    function togglePermissionSection() {
        permissionSection.style.display = userTypeSelect.value === 'Admin' ? 'none' : 'block';
    }
    userTypeSelect.addEventListener('change', togglePermissionSection);

    /**
     * Fetches users from the API based on the search keyword and renders them in the table.
     */
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

            let actionButtons = '';
            if (userPermissions.canEdit) {
                actionButtons += `<a href="#" class="action-link me-3 edit-btn" data-id="${user.userId}">Edit</a>`;
            }
            if (userPermissions.canDelete) {
                actionButtons += user.activeFlag
                    ? `<a href="#" class="action-link text-danger deactivate-btn" data-id="${user.userId}">De-active</a>`
                    : `<a href="#" class="action-link text-success activate-btn" data-id="${user.userId}">Active</a>`;
            }

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

    /**
     * Fetches the data for a single user and populates the modal form for editing.
     * @param {number} userId - The ID of the user to edit.
     */
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

        togglePermissionSection();
        document.querySelectorAll('.permission-checkbox').forEach(cb => cb.checked = false);
        if (user.permissions) {
            user.permissions.forEach(permissionName => {
                const checkbox = document.getElementById(`perm_${permissionName}`);
                if (checkbox) {
                    checkbox.checked = true;
                }
            });
        }

        userModal.show();
    }

    /**
     * Gathers data from the form, creates a JSON payload, and sends it to the server
     * to create a new user or update an existing one.
     */
    async function saveUser() {

        const userName = document.getElementById('userName').value.trim();
        const phoneNumber = document.getElementById('phoneNumber').value.trim();
        const department = document.getElementById('department').value.trim();
        const userType = document.getElementById('userType').value;

        if (!userName || !phoneNumber || !department || !userType) {
            Swal.fire({
                icon: 'error',
                title: 'Validation Error',
                text: 'Please fill in all required fields (*).',
                confirmButtonColor: '#384295'
            });
            return;
        }

        if (userType === 'Normal') {
            const dashboardViewCheckbox = document.getElementById('perm_DASHBOARD_VIEW');
            if (dashboardViewCheckbox && !dashboardViewCheckbox.checked) {
                Swal.fire({
                    icon: 'error',
                    title: 'Validation Error',
                    text: 'A "Normal" user must have the "Dashboard: View" permission.',
                    confirmButtonColor: '#384295'
                });
                return;
            }
        }



        const password = document.getElementById('password').value;
        const userData = {
            userId: document.getElementById('userId').value || null,
            userName: userName,
            phoneNumber: phoneNumber,
            department: department,
            productionLine: document.getElementById('productionLine').value,
            emailAddress: document.getElementById('emailAddress').value,
            userType: userType
        };

        if (userType === 'Normal') {
            const selectedPermissions = [];
            document.querySelectorAll('.permission-checkbox:checked').forEach(checkbox => {
                selectedPermissions.push(checkbox.value);
            });
            userData.permissions = selectedPermissions;
        }

        if (password) {
            userData.password = password;
        }

        // Prepare headers and add the CSRF token.
        const headers = {
            'Content-Type': 'application/json'
        };
        headers[csrfToken.headerName] = csrfToken.token;

        const response = await fetch('/api/users', {
            method: 'POST',
            headers: headers, // Use the headers object with the token
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
        userTypeSelect.value = 'Normal';
        togglePermissionSection();
        userModal.show();
    });

    saveUserBtn.addEventListener('click', saveUser);

    userTableBody.addEventListener('click', async (e) => {
        e.preventDefault();
        const target = e.target;
        const userId = target.dataset.id;

        if (!userId) return;

        const headers = {};
        headers[csrfToken.headerName] = csrfToken.token;

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
                confirmButtonText: 'De-Active'
            });
            if (result.isConfirmed) {
                await fetch(`/api/users/${userId}/deactivate`, { method: 'POST', headers: headers });
                fetchUsers();
            }
        } else if (target.classList.contains('activate-btn')) {
            await fetch(`/api/users/${userId}/activate`, { method: 'POST', headers: headers });
            fetchUsers();
        }
    });

    /**
     * Initial data load when the page is ready.
     */
    fetchUsers();
});