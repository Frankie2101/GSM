document.addEventListener('DOMContentLoaded', function () {
    // Lấy các element cần thiết
    const addBtn = document.getElementById('add-color-btn');
    const deleteSelectedBtn = document.getElementById('delete-selected-btn');
    const tableBody = document.getElementById('fabricColorTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    // HÀM REINDEX ĐÃ SỬA LỖI HOÀN CHỈNH
    const reindexRows = () => {
        if (!tableBody) return;
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input');
            inputs.forEach(input => {
                // Lấy tên gốc từ thuộc tính `data-name`
                const dataName = input.getAttribute('data-name');
                if (dataName) {
                    // Gán lại tên theo đúng định dạng Spring Boot yêu cầu
                    input.name = `fabricColors[${index}].${dataName}`;
                }
            });
        });
    };

    // --- CÁC HÀNH ĐỘNG CỦA NGƯỜI DÙNG ---

    // Khi nhấn nút "New Detail"
    if (addBtn) {
        addBtn.addEventListener('click', function () {
            const newRowHtml = `
                <tr>
                    <td class="text-center align-middle">
                        <input class="form-check-input row-checkbox" type="checkbox">
                    </td>
                    <input type="hidden" data-name="fabricColorId" value="">
                    <td><input type="text" data-name="color" class="form-control" required></td>
                    <td><input type="text" data-name="colorName" class="form-control"></td>
                    <td><input type="text" data-name="width" class="form-control"></td>
                    <td><input type="number" step="0.01" data-name="netPrice" class="form-control"></td>
                    <td><input type="number" step="0.01" data-name="taxPercent" class="form-control"></td>
                    <td class="text-center align-middle">
                        <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            if (tableBody) {
                tableBody.insertAdjacentHTML('beforeend', newRowHtml);
                // QUAN TRỌNG: Gọi lại hàm reindex để gán `name` cho dòng mới
                reindexRows();
            }
        });
    }

    // Khi nhấn nút "Delete" (xóa các dòng đã chọn)
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function () {
            if (!tableBody) return;
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({ icon: 'error', title: 'No Colors Selected', text: 'Please select at least one color to delete.' });
                return;
            }
            checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
            // QUAN TRỌNG: Gọi lại hàm reindex sau khi xóa
            reindexRows();
        });
    }

    // Xử lý nút xóa trên từng dòng
    if (tableBody) {
        tableBody.addEventListener('click', function (e) {
            if (e.target.closest('.delete-row-btn')) {
                e.target.closest('tr').remove();
                // QUAN TRỌNG: Gọi lại hàm reindex sau khi xóa
                reindexRows();
            }
        });
    }

    // Xử lý checkbox "Chọn tất cả"
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            if (!tableBody) return;
            tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // QUAN TRỌNG: Gọi reindex một lần khi trang được tải để gán `name` cho các dòng đã có sẵn
    reindexRows();
});