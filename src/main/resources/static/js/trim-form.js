document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('addVariantBtn');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const tableBody = document.getElementById('variantTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const trimForm = document.getElementById('trimForm');

    const reindexRows = () => {
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input');
            inputs.forEach(input => {
                const dataName = input.getAttribute('data-name');
                if (dataName) {
                    input.name = `variants[${index}].${dataName}`;
                }
            });
        });
    };

    if (addBtn) {
        addBtn.addEventListener('click', function() {
            const newIndex = tableBody.rows.length;
            const newRowHtml = `
                <tr>
                    <td class="text-center align-middle"><input class="form-check-input row-checkbox" type="checkbox"></td>
                    <input type="hidden" data-name="trimVariantId" value="">
                    <td><input type="text" data-name="colorCode" class="form-control" required></td>
                    <td><input type="text" data-name="colorName" class="form-control"></td>
                    <td><input type="text" data-name="sizeCode" class="form-control" placeholder="e.g., 120,180,200" required></td>
                    <td><input type="number" step="0.01" data-name="netPrice" class="form-control"></td>
                    <td><input type="number" step="0.01" data-name="taxRate" class="form-control"></td>
                    <td class="text-center align-middle">
                        <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn"><i class="bi bi-trash"></i></button>
                    </td>
                </tr>
            `;
            tableBody.insertAdjacentHTML('beforeend', newRowHtml);
            reindexRows(); // Gọi sau khi thêm dòng mới
        });
    }

    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({ icon: 'error', title: 'No Variants Selected', text: 'Please select at least one variant to delete.' });
                return;
            }
            checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
            reindexRows(); // Gọi sau khi xóa
        });
    }

    if (tableBody) {
        tableBody.addEventListener('click', function(e) {
            if (e.target.closest('.delete-row-btn')) {
                e.target.closest('tr').remove();
                reindexRows(); // Gọi sau khi xóa
            }
        });
    }

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                this.checked ? checkbox.checked = true : checkbox.checked = false;
            });
        });
    }

    // Gán lại name cho các dòng đã có sẵn khi tải trang
    reindexRows();
});
