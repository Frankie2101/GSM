/**
 * @file Manages the Production Output list page.
 * Handles fetching, displaying, creating, editing, and deleting production output records.
 */
document.addEventListener('DOMContentLoaded', function () {

    // --- Elements Caching ---
    // Caching DOM elements for better performance and easier access.
    /**
     * @class ProductionOutputPage
     * @description An object to encapsulate all functionality for the page.
     */
    const createBtn = document.getElementById('createBtn');
    const outputTableBody = document.getElementById('outputTableBody');
    const outputModal = new bootstrap.Modal(document.getElementById('outputModal'));
    const outputForm = document.getElementById('outputForm');
    const saleOrderNoInput = document.getElementById('saleOrderNo');
    const styleSelect = document.getElementById('style');
    const colorSelect = document.getElementById('color');
    const departmentInput = document.getElementById('modalDepartment');
    const productionLineInput = document.getElementById('modalProductionLine');

    // --- State Management ---
    let saleOrderDetailsCache = [];
    let currentUser = null;

    // --- LOGIC FUNCTIONS ---

    /**
     * Fetches the current logged-in user's information and caches it.
     * This function is designed to run only once to avoid unnecessary API calls.
     */
    async function loadCurrentUser() {
        if (currentUser) return; // Chỉ tải 1 lần
        try {
            const response = await fetch('/api/users/me');
            if (response.ok) {
                currentUser = await response.json();
            } else {
                console.error('Could not fetch current user info.');
            }
        } catch (error) {
            console.error('Error fetching current user:', error);
        }
    }

    /**
     * Handles the logic when the Sale Order No. input loses focus (on blur).
     * It fetches SO details, populates the 'Style' dropdown, and can trigger a callback.
     * @param {Function} callback - A function to execute after the details have been loaded, used for the edit flow.
     */
    async function handleSaleOrderNoChange(callback) {
        const soNo = saleOrderNoInput.value.trim();
        styleSelect.innerHTML = '<option value="">-- Loading... --</option>';
        colorSelect.innerHTML = '<option value="">-- Select Style first --</option>';

        if (!soNo) {
            styleSelect.innerHTML = '<option value="">-- Type SO No first --</option>';
            return;
        }

        try {
            // Fetch all style/color combinations for the given SO.
            const response = await fetch(`/api/sale-orders/${soNo}/details`);
            const details = await response.json();
            saleOrderDetailsCache = details;

            const styles = [...new Set(details.map(d => d.style))];

            populateSelect(styleSelect, styles, 'Select Style');

            // Automatically select if there is only one style.
            if (styles.length === 1) {
                styleSelect.value = styles[0];
                handleStyleChange();
            }

            // If a callback was passed (e.g., from openEditModal), execute it.
            if (callback) callback();
        } catch (error) {
            console.error('Error fetching SO details:', error);
            styleSelect.innerHTML = '<option value="">-- Error loading --</option>';
        }
    }

    /**
     * Handles the logic when the 'Style' dropdown selection changes.
     * It populates the 'Color' dropdown based on the selected style using the cached data.
     */
    function handleStyleChange() {
        const selectedStyle = styleSelect.value;
        if (!selectedStyle) {
            colorSelect.innerHTML = '<option value="">-- Select Style first --</option>';
            return;
        }

        // Filter the cached details to get colors for the selected style.
        const colors = saleOrderDetailsCache
            .filter(d => d.style === selectedStyle)
            .map(d => d.color);
        const uniqueColors = [...new Set(colors)];

        populateSelect(colorSelect, uniqueColors, 'Select Color');

        if (uniqueColors.length === 1) {
            colorSelect.value = uniqueColors[0];
        }
    }

    // --- Event Listeners ---

    /**
     * Event listener for the "Create" button.
     * It resets the modal form, sets default values, pre-fills user info, and shows the modal.
     */
    if (createBtn) {
        createBtn.addEventListener('click', async () => {
            outputForm.reset();
            styleSelect.innerHTML = '<option value=\"\">-- Type SO No first --</option>';
            colorSelect.innerHTML = '<option value=\"\">-- Select Style first --</option>';
            document.getElementById('outputModalLabel').textContent = 'Create Production Output';
            document.getElementById('productionOutputId').value = '';
            document.getElementById('outputDate').valueAsDate = new Date();

            await loadCurrentUser();
            if (currentUser) {
                departmentInput.value = currentUser.department || '';
                productionLineInput.value = currentUser.productionLine || '';
            }

            outputModal.show();
        });
    }

    // Add event listeners for the chained select functionality.
    saleOrderNoInput.addEventListener('blur', () => handleSaleOrderNoChange(null));
    styleSelect.addEventListener('change', handleStyleChange);

    /**
     * A helper function to populate a <select> element with options.
     * @param {HTMLElement} selectElement - The <select> element to populate.
     * @param {Array<string>} options - An array of strings for the options.
     * @param {string} placeholder - The placeholder text for the default option.
     */
    function populateSelect(selectElement, options, placeholder) {
        selectElement.innerHTML = `<option value="">-- ${placeholder} --</option>`;
        options.forEach(option => {
            const optionElement = document.createElement('option');
            optionElement.value = option;
            optionElement.textContent = option;
            selectElement.appendChild(optionElement);
        });
    }

    // --- Other Functions and Event Listeners ---
    const searchBtn = document.getElementById('searchBtn');
    const deleteBtn = document.getElementById('deleteBtn');
    const saveBtn = document.getElementById('saveBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const csrfTokenInput = document.getElementById('csrfToken');
    const csrfHeader = csrfTokenInput ? csrfTokenInput.getAttribute('name') : '_csrf';
    const csrfToken = csrfTokenInput ? csrfTokenInput.getAttribute('value') : '';

    /**
     * Fetches production outputs from the server based on search criteria and renders them.
     */
    async function fetchOutputs() {
        const keyword = document.getElementById('keyword').value;
        const outputDateFrom = document.getElementById('outputDateFrom').value;
        const outputDateTo = document.getElementById('outputDateTo').value;
        const department = document.getElementById('department').value;
        const productionLine = document.getElementById('productionLine').value;

        const queryParams = new URLSearchParams({ keyword, outputDateFrom, outputDateTo, department, productionLine });
        const url = `/api/production-outputs?${queryParams}`;

        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error('Network response was not ok');
            const outputs = await response.json();
            renderTable(outputs);
        } catch (error) {
            console.error('Fetch error:', error);
            outputTableBody.innerHTML = `<tr><td colspan="11" class="text-center text-danger p-4">Error loading data.</td></tr>`;
        }
    }

    /**
     * Renders an array of output objects into the main table.
     * @param {Array} outputs - The array of production output data.
     */
    function renderTable(outputs) {
        outputTableBody.innerHTML = '';
        if (outputs.length === 0) {
            outputTableBody.innerHTML = `<tr><td colspan="11" class="text-center text-muted p-4">No production outputs found.</td></tr>`;
            return;
        }
        outputs.forEach(output => {
            const row = `
                <tr>
                    <td class="text-center align-middle"><input class="form-check-input row-checkbox" type="checkbox" value="${output.productionOutputId}"></td>
                    <td class="p-3 align-middle">${output.sequenceNumber}</td>
                    <td class="p-3 align-middle">${output.saleOrderNo || ''}</td>
                    <td class="p-3 align-middle">${output.style || ''}</td>
                    <td class="p-3 align-middle">${output.color || ''}</td>
                    <td class="p-3 align-middle">${output.department || ''}</td>
                    <td class="p-3 align-middle">${output.productionLine || ''}</td>
                    <td class="py-3 ps-2 align-middle">${output.outputDate || ''}</td>
                    <td class="py-3 ps-2 align-middle text-end">${output.outputQuantity || 0}</td>
                    <td class="p-3 align-middle">${output.createdBy || ''}</td>
                    <td class="p-3 align-middle">
                        <button type="button" class="btn btn-sm btn-link-custom edit-btn" data-id="${output.productionOutputId}">Edit</button>
                    </td>
                </tr>
            `;
            outputTableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    /**
     * Opens the modal to edit an existing record, fetching its data first.
     * @param {number} id - The ID of the record to edit.
     */
    async function openEditModal(id) {
        try {
            const response = await fetch(`/api/production-outputs/${id}`);
            if (!response.ok) throw new Error('Failed to fetch data');
            const data = await response.json();

            document.getElementById('outputModalLabel').textContent = 'Edit Production Output';
            outputForm.querySelector('#productionOutputId').value = data.productionOutputId;
            outputForm.querySelector('#saleOrderNo').value = data.saleOrderNo;
            outputForm.querySelector('#outputDate').value = data.outputDate;
            outputForm.querySelector('#outputQuantity').value = data.outputQuantity;

            departmentInput.value = data.department || '';
            productionLineInput.value = data.productionLine || '';

            // Load the SO details, then set style, then load colors, then set color.
            await handleSaleOrderNoChange(() => {
                styleSelect.value = data.style;
                handleStyleChange();
                // A timeout is used to ensure the color dropdown is populated by the 'change' event before setting its value.
                setTimeout(() => { colorSelect.value = data.color; }, 100);
            });

            outputModal.show();
        } catch (error) {
            console.error('Error in openEditModal:', error);
            Swal.fire('Error', 'Could not fetch data for editing.', 'error');
        }
    }

    /**
     * Saves the data from the modal form (both for create and update).
     */
    async function saveOutput() {
        const dto = {
            productionOutputId: document.getElementById('productionOutputId').value || null,
            saleOrderNo: document.getElementById('saleOrderNo').value,
            outputDate: document.getElementById('outputDate').value,
            style: document.getElementById('style').value,
            color: document.getElementById('color').value,
            department: departmentInput.value,
            productionLine: productionLineInput.value,
            outputQuantity: document.getElementById('outputQuantity').value
        };

        try {
            const response = await fetch('/api/production-outputs/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                body: JSON.stringify(dto)
            });

            if (response.ok) {
                outputModal.hide();
                await fetchOutputs();
                Swal.fire('Success', 'Saved successfully!', 'success');
            } else {
                const error = await response.json();
                throw new Error(error.message || 'Failed to save');
            }
        } catch (error) {
            console.error('Error in saveOutput:', error);
            Swal.fire('Error', error.message, 'error');
        }
    }


    /**
     * Deletes all selected records after a confirmation dialog.
     */
    async function deleteSelectedOutputs() {
        const selectedIds = Array.from(document.querySelectorAll('.row-checkbox:checked')).map(cb => cb.value);
        if (selectedIds.length === 0) {
            Swal.fire('No Selection', 'Please select at least one item to delete.', 'warning');
            return;
        }

        const result = await Swal.fire({
            title: 'Are you sure?',
            text: `You are about to delete ${selectedIds.length} item(s). You won't be able to revert this!`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            confirmButtonText: 'Delete'
        });

        if (result.isConfirmed) {
            try {
                const response = await fetch('/api/production-outputs/delete', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
                    body: JSON.stringify(selectedIds)
                });
                if (response.ok) {
                    await fetchOutputs(); // Refresh the table.
                    Swal.fire('Deleted!', 'The selected items have been deleted.', 'success');
                } else {
                    const error = await response.json();
                    throw new Error(error.message);
                }
            } catch(error) {
                Swal.fire('Error', error.message, 'error');
            }
        }
    }

    // Assign event listeners to the main action buttons and the table.
    searchBtn.addEventListener('click', fetchOutputs);
    saveBtn.addEventListener('click', saveOutput);

    if (deleteBtn) {
        deleteBtn.addEventListener('click', deleteSelectedOutputs);
    }

    // Use event delegation for the edit buttons inside the table.
    outputTableBody.addEventListener('click', function(e) {
        if (e.target && e.target.classList.contains('edit-btn')) {
            openEditModal(e.target.dataset.id);
        }
    });

    // Listener for the "select all" checkbox.
    selectAllCheckbox.addEventListener('change', function() {
        document.querySelectorAll('.row-checkbox').forEach(checkbox => checkbox.checked = this.checked);
    });

    fetchOutputs();

});