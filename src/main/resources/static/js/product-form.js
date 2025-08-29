document.addEventListener('DOMContentLoaded', function () {

    const addVariantBtn = document.getElementById('add-variant-btn');
    const deleteSelectedBtn = document.getElementById('delete-selected-btn');
    // SỬA LỖI: ID của tbody phải là 'variantTableBody' (không có gạch ngang)
    // Hãy đảm bảo trong file product_form.mustache của bạn, thẻ <tbody> có id="variantTableBody"
    const tableBody = document.getElementById('variantTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    const reindexRows = () => {
        if (!tableBody) return; // Thoát nếu không tìm thấy table body
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input, select');
            inputs.forEach(input => {
                // Lấy ra giá trị của thuộc tính 'name' ban đầu, ví dụ: "color", "size"
                const nameAttr = input.getAttribute('data-name');
                if (nameAttr) {
                    // Gán lại tên theo đúng định dạng mà Spring Boot yêu cầu
                    input.name = `variants[${index}].${nameAttr}`;
                }
            });
        });
    };

    // Gọi reindexRows() sau khi trang được tải để xử lý các hàng đã có
    reindexRows();

    if (addVariantBtn) {
        addVariantBtn.addEventListener('click', function () {
            const newRowHtml = `
                <tr>
                    <td class="text-center align-middle">
                        <input class="form-check-input row-checkbox" type="checkbox">
                    </td>
                    <input type="hidden" data-name="productVariantId" value="">
                    <td><input type="text" data-name="color" class="form-control" required></td>
                    <td><input type="text" data-name="size" class="form-control" placeholder="e.g., XS,S,M" required></td>
                    <td><input type="number" step="0.01" data-name="price" class="form-control"></td>
                    <td><input type="text" data-name="currency" class="form-control" value="VND" maxlength="3"></td>
                    <td class="text-center align-middle">
                        <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            if(tableBody) {
                tableBody.insertAdjacentHTML('beforeend', newRowHtml);
                reindexRows();
            }
        });
    }

    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            if (!tableBody) return;
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                // Sử dụng SweetAlert cho đồng bộ
                Swal.fire({
                    icon: 'error',
                    title: 'No variants selected',
                    text: 'Please select at least one variant to delete.'
                });
                return;
            }
            checkedBoxes.forEach(checkbox => {
                checkbox.closest('tr').remove();
            });
            reindexRows();
        });
    }

    if (tableBody) {
        tableBody.addEventListener('click', function (e) {
            const deleteButton = e.target.closest('.delete-row-btn');
            if (deleteButton) {
                deleteButton.closest('tr').remove();
                reindexRows();
            }
        });
    }

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            if (!tableBody) return;
            const checkboxesInBody = tableBody.querySelectorAll('.row-checkbox');
            checkboxesInBody.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }
});