document.addEventListener('DOMContentLoaded', function () {
    const tableBody = document.getElementById('pending-po-table-body');

    // --- CÁC HÀM API ---
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
                    <a href="/purchase_orders/print/${po.purchaseOrderId}" class="btn btn-sm btn-outline-primary">
                        <i class="bi bi-eye-fill"></i> View
                    </a>
                </td>
            </tr>
        `;
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // --- XỬ LÝ SỰ KIỆN ---
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
                // Tải lại danh sách sau khi hành động thành công
                loadPendingPOs();
            } catch (error) {
                // Lỗi đã được hiển thị bởi fetchApi
            }
        }
    }

    // --- HÀM TẢI DỮ LIỆU BAN ĐẦU ---
    async function loadPendingPOs() {
        tableBody.innerHTML = '<tr><td colspan="9" class="text-center p-5">Loading...</td></tr>';
        try {
            const pendingPOs = await fetchApi('/api/purchase_orders/pending');
            renderTable(pendingPOs);
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="9" class="text-center text-danger p-5">Failed to load data. Please try again.</td></tr>';
        }
    }

    // --- KHỞI CHẠY ---
    loadPendingPOs();
});