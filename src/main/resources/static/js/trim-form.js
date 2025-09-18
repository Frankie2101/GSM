/**
 * @fileoverview Manages the dynamic and interactive behavior of the trim_form.mustache page.
 * Handles client-side CRUD for trim variants and re-indexes input names for Spring MVC binding.
 */
document.addEventListener('DOMContentLoaded', function() {
    // --- Element Selectors ---
    const addBtn = document.getElementById('addVariantBtn');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const tableBody = document.getElementById('variantTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const trimForm = document.getElementById('trimForm');

    /**
     * Re-indexes the `name` attributes of all variant inputs.
     * This is critical for Spring MVC to bind the form data to a List<TrimVariantDto>.
     * The names must be in the format `variants[index].fieldName`.
     */
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

    /**
     * Event listener for the "New Detail" button.
     * Appends a new, empty row to the variants table.
     */
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
            reindexRows();
        });
    }

    /**
     * Event listener for the "Delete Selected" button.
     * Removes all rows that have their checkbox checked.
     */
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({ icon: 'error', title: 'No Variants Selected', text: 'Please select at least one variant to delete.' });
                return;
            }
            checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
            reindexRows();
        });
    }

    /**
     * Event listener for individual delete buttons on each row using event delegation.
     */
    if (tableBody) {
        tableBody.addEventListener('click', function(e) {
            if (e.target.closest('.delete-row-btn')) {
                e.target.closest('tr').remove();
                reindexRows();
            }
        });
    }

    /**
     * Event listener for the "Select All" checkbox.
     * Toggles the state of all checkboxes in the table body.
     */
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                this.checked ? checkbox.checked = true : checkbox.checked = false;
            });
        });
    }

    // Initial call to set names for any pre-existing rows on page load.
    reindexRows();
});
