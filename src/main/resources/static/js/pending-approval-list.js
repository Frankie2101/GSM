/**
 * @fileoverview Manages the "Pending Approval POs" list page.
 * It fetches the list of pending POs from an API and renders them into a table.
 */
document.addEventListener('DOMContentLoaded', function () {
    // --- 1. INITIALIZATION & ELEMENT SELECTORS ---
    const tableBody = document.getElementById('pending-po-table-body');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // --- 2. API HELPERS ---
    /**
     * A generic fetch wrapper to handle CSRF tokens and response parsing.
     */
    async function fetchApi(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: { 'Content-Type': 'application/json' },
                ...options
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `API call failed: ${response.status}`);
            }
            if (response.status === 204 || (response.status === 200 && options.method === 'POST')) return null;
            return await response.json();
        } catch (error) {
            Swal.fire('API Error', error.message, 'error');
            throw error;
        }
    }

    // --- 3. UI RENDERING ---
    function renderTable(pos) {
        tableBody.innerHTML = '';
        if (!pos || pos.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="9" class="text-center p-5">No purchase orders are currently pending approval.</td></tr>';
            return;
        }

        pos.forEach((po, index) => {
            const formattedTotalAmount = (po.totalAmount || 0).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            const row = `
            <tr data-po-id="${po.purchaseOrderId}">
                <td>${index + 1}</td>
                <td><strong>${po.purchaseOrderNo}</strong></td>
                <td>${po.supplierName || 'N/A'}</td>
                <td>${po.poDate ? new Date(po.poDate).toLocaleDateString() : 'N/A'}</td>
                <td>${po.arrivalDate ? new Date(po.arrivalDate).toLocaleDateString() : 'N/A'}</td>
                <td class="text-end">${formattedTotalAmount}</td>
                <td>${po.currencyCode || 'N/A'}</td>
                <td><span class="badge bg-warning text-dark">${po.status}</span></td>
                <td class="text-center">
                    <a href="/purchase_orders/print/${po.purchaseOrderId}" class="action-link">View</a>
                </td>
            </tr>
        `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    tableBody.addEventListener('click', function(event) {
        const target = event.target;
        const poId = target.closest('tr')?.dataset.poId;

        if (!poId) return;
        if (target.classList.contains('approve-btn')) {
            handleApproval(poId, 'approve');
        } else if (target.classList.contains('reject-btn')) {
            handleApproval(poId, 'reject');
        }
    });

    async function handleApproval(poId, action) {
        const isApproving = action === 'approve';
        const result = await Swal.fire({
            title: `${isApproving ? 'Approve' : 'Reject'} Purchase Order?`,
            text: `Are you sure you want to ${action} this PO?`,
            icon: 'question',
            showCancelButton: true,
            confirmButtonColor: isApproving ? '#198754' : '#dc3545',
            confirmButtonText: `${action}`
        });

        if (result.isConfirmed) {
            try {
                await fetchApi(`/api/purchase-orders/${poId}/${action}`, { method: 'POST' });
                Swal.fire(
                    isApproving ? 'Approved!' : 'Rejected!',
                    `The PO has been ${action}d.`,
                    'success'
                );
                loadPendingPOs();
            } catch (error) {
            }
        }
    }

    // --- 4. DATA LOADING ---
    async function loadPendingPOs() {
        // Step 1: Display a "Loading..." message.
        tableBody.innerHTML = '<tr><td colspan="9" class="text-center p-5">Loading...</td></tr>';
        try {
            // Step 2: Call the API endpoint for pending POs.
            const pendingPOs = await fetchApi('/api/purchase_orders/pending');
            // Step 3: Render the table with the fetched data.
            renderTable(pendingPOs);
        } catch (error) {
            // Step 4: If the API fails, show an error message.
            tableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger p-5">Failed to load data. Please try again.</td></tr>';
        }
    }

    // --- 5. INITIALIZATION ---
    loadPendingPOs();
});