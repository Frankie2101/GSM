document.addEventListener('DOMContentLoaded', function () {
    // --- KHAI BÁO CÁC ELEMENT ---
    const tableBody = document.getElementById('po-table-body');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const searchInput = document.getElementById('searchInput'); // Element ô tìm kiếm
    const searchBtn = document.getElementById('searchBtn');     // Element nút tìm kiếm

    let allPOs = []; // Biến để lưu trữ toàn bộ danh sách PO gốc

    // --- HÀM GỌI API (giữ nguyên) ---
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

    // --- HÀM RENDER BẢNG (giữ nguyên) ---
    function renderTable(pos) {
        tableBody.innerHTML = '';
        if (!pos || pos.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5">No purchase orders found.</td></tr>';
            return;
        }

        pos.forEach((po, index) => {
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

            const formattedTotalAmount = (po.totalAmount || 0).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
            });

            const canBeDeleted = po.status === 'New' || po.status === 'Rejected';

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
            tableBody.insertAdjacentHTML('beforeend', row);
        });
    }

    // --- LOGIC MỚI CHO CHỨC NĂNG SEARCH ---
    function performSearch() {
        const searchTerm = searchInput.value.toLowerCase().trim();

        if (!searchTerm) {
            renderTable(allPOs); // Nếu ô tìm kiếm rỗng, hiển thị lại toàn bộ danh sách
            return;
        }

        // Lọc danh sách PO dựa trên searchTerm
        const filteredPOs = allPOs.filter(po => {
            const poNumber = po.purchaseOrderNo ? po.purchaseOrderNo.toLowerCase() : '';
            const supplier = po.supplierName ? po.supplierName.toLowerCase() : '';

            // Trả về true nếu PO Number hoặc Supplier Name chứa từ khóa tìm kiếm
            return poNumber.includes(searchTerm) || supplier.includes(searchTerm);
        });

        renderTable(filteredPOs); // Hiển thị kết quả đã lọc
    }

    // --- CÁC HÀM XỬ LÝ SỰ KIỆN ---

    // Gán sự kiện cho nút Search
    searchBtn.addEventListener('click', performSearch);

    // Gán sự kiện để có thể tìm kiếm bằng cách nhấn Enter
    searchInput.addEventListener('keyup', function(event) {
        if (event.key === 'Enter') {
            performSearch();
        }
    });

    // Các sự kiện xóa và chọn tất cả (giữ nguyên)
    deleteSelectedBtn.addEventListener('click', function() {
        // ... code xóa giữ nguyên ...
    });

    selectAllCheckbox.addEventListener('change', function() {
        // ... code chọn tất cả giữ nguyên ...
    });

    // --- HÀM TẢI DỮ LIỆU ---
    async function loadPOs() {
        tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5">Loading data...</td></tr>';
        try {
            allPOs = await fetchApi('/api/purchase_orders'); // Lưu dữ liệu gốc vào biến allPOs
            renderTable(allPOs); // Hiển thị lần đầu
        } catch (error) {
            tableBody.innerHTML = '<tr><td colspan="12" class="text-center p-5 text-danger">An error occurred while loading data.</td></tr>';
        }
    }

    // --- KHỞI CHẠY ---
    loadPOs();
});