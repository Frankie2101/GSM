/**
 * @fileoverview A client-side script that fetches, renders, and manages the Purchase Order list page.
 * It handles all user interactions like searching, selecting, and deleting POs.
 */
document.addEventListener('DOMContentLoaded', function () {
    // --- 1. INITIALIZATION & ELEMENT SELECTORS ---
    const tableBody = document.getElementById('po-table-body');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const searchInput = document.getElementById('searchInput');
    const searchBtn = document.getElementById('searchBtn');

    let allPOs = []; // A cache to store the master list of all POs to enable client-side search.

    // --- 2. API HELPERS ---

    /**
     * A generic fetch wrapper to automatically handle CSRF headers and response parsing.
    */
    async function fetchApi(url, options = {}) {
        try {
            const response = await fetch(url, options);
            if (!response.ok) throw new Error(`API call failed: ${response.status}`);
            if (response.status === 204 || (response.status === 200 && options.method === 'DELETE')) return;
            return await response.json();
        } catch (error) {
            console.error('API Error:', error);
            Swal.fire('Error', 'An error occurred while communicating with the server.', 'error');
            throw error;
        }
    }

    // --- 3. UI RENDERING FUNCTIONS ---
    function renderTable(pos) {
        // 1. Clear the current table content.
        tableBody.innerHTML = '';
        // 2. Handle the case where there is no data.
        if (!pos || pos.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5">No purchase orders found.</td></tr>';
            return;
        }

        // 3. Loop through each Purchase Order object in the data array.
        pos.forEach((po, index) => {
            // Determine the correct Bootstrap badge class based on the PO's status.
            let statusBadge;
            switch(po.status) {
                case 'New':
                    statusBadge = '<span class="badge bg-light text-success border border-success">New</span>';
                    break;
                case 'Approved':
                    statusBadge = '<span class="badge bg-success">Approved</span>';
                    break;
                case 'Rejected':
                    statusBadge = '<span class="badge bg-danger">Rejected</span>';
                    break;
                case 'Submitted':
                    statusBadge = '<span class="badge bg-warning text-dark">Submitted</span>';
                    break;
                default:
                    statusBadge = `<span class="badge bg-secondary">${po.status}</span>`;
            }

            // Format the total amount into a currency string with two decimal places.
            const formattedTotalAmount = (po.totalAmount || 0).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            // Determine if the row's checkbox should be enabled or disabled.
            const canBeDeleted = po.status === 'New' || po.status === 'Rejected';

            // Build the final HTML string for the entire table row.
            const row = `
                <tr>
                    <td><input class="form-check-input row-checkbox" type="checkbox" value="${po.purchaseOrderId}" ${!canBeDeleted ? 'disabled' : ''}></td>
                    <td>${index + 1}</td>
                    <td><strong>${po.purchaseOrderNo}</strong></td>
                    <td>${po.supplierName || 'N/A'}</td>
                    <td>${po.poDate ? new Date(po.poDate).toLocaleDateString() : 'N/A'}</td>
                    <td>${po.arrivalDate ? new Date(po.arrivalDate).toLocaleDateString() : 'N/A'}</td>
                    <td>${po.currencyCode || 'N/A'}</td>
                    <td>${po.deliveryTerm || 'N/A'}</td>
                    <td>${po.paymentTerm || 'N/A'}</td>
                    <td class="text-end">${formattedTotalAmount}</td>
                    <td>${statusBadge}</td>
                    <td class="text-center">
                        <a href="/purchase_orders/form?id=${po.purchaseOrderId}" class="action-link">View</a>
                    </td>
                </tr>
            `;
            // Insert the newly created row HTML into the table body.
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // --- 4. CORE LOGIC FUNCTIONS ---

    /**
     * Performs a client-side search by filtering the master `allPOs` array.
     */
    function performSearch() {
        // 1. Get the search term from the input, convert to lowercase.
        const searchTerm = searchInput.value.toLowerCase().trim();

        // 2. If search term is empty, render the full, original list.
        if (!searchTerm) {
            renderTable(allPOs);
            return;
        }

        // 3. Filter the `allPOs` array in memory.
        const filteredPOs = allPOs.filter(po => {
            const poNumber = po.purchaseOrderNo ? po.purchaseOrderNo.toLowerCase() : '';
            const supplier = po.supplierName ? po.supplierName.toLowerCase() : '';

            return poNumber.includes(searchTerm) || supplier.includes(searchTerm);
        });

        // 4. Render the table with only the filtered results.
        renderTable(filteredPOs);
    }


    searchBtn.addEventListener('click', performSearch);

    searchInput.addEventListener('keyup', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });

    deleteSelectedBtn.addEventListener('click', function() {
        const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
        const count = checkedBoxes.length;

        if (count === 0) {
            Swal.fire({
                icon: 'warning',
                title: 'No Items Selected',
                text: 'Please select at least one Purchase Order to delete.',
            });
            return;
        }

        Swal.fire({
            title: `Delete ${count} Purchase Order(s)?`,
            text: "This action cannot be undone!",
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: 'Delete'
        }).then((result) => {
            if (result.isConfirmed) {
                checkedBoxes.forEach(box => {
                    const hiddenInput = document.createElement('input');
                    hiddenInput.type = 'hidden';
                    hiddenInput.name = 'selectedIds';
                    hiddenInput.value = box.value;
                    deleteForm.appendChild(hiddenInput);
                });
                deleteForm.submit();
            }
        });
    });

    selectAllCheckbox.addEventListener('change', function() {
        const rowCheckboxes = tableBody.querySelectorAll('.row-checkbox:not(:disabled)');
        rowCheckboxes.forEach(checkbox => {
            checkbox.checked = this.checked;
        });
    });

    async function loadPOs() {
        // 1. Show a "Loading..." message to the user.
        tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5">Loading data...</td></tr>';
        try {
            // 2. Fetch all POs from the API.
            allPOs = await fetchApi('/api/purchase_orders');

            // 3. Render the full table with the fetched data.
            renderTable(allPOs);
        } catch (error) {
            // 4. If the API fails, show an error message in the table.
            tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5 text-danger">An error occurred while loading data.</td></tr>';
        }
    }

    // --- INITIALIZATION ---
    loadPOs();
});