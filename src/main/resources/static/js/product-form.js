/**
 * @fileoverview This script manages the dynamic behavior of the product_form.mustache page.
 * It handles client-side CRUD operations for the product variants table, including adding new rows,
 * deleting selected rows, and re-indexing form input names for proper data binding in Spring MVC.
 */
document.addEventListener('DOMContentLoaded', function () {

    // --- Element Selectors ---
    const addVariantBtn = document.getElementById('add-variant-btn');
    const deleteSelectedBtn = document.getElementById('delete-selected-btn');
    const tableBody = document.getElementById('variantTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    /**
     * Re-indexes the `name` attributes of all input fields within the variants table.
     * This is to correctly bind the list of variants.
     * The names must be in the format `variants[index].fieldName`.
     * For example: `variants[0].color`, `variants[1].color`, etc.
     */
    const reindexRows = () => {
        if (!tableBody) return;
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input, select');
            inputs.forEach(input => {
                const nameAttr = input.getAttribute('data-name');
                if (nameAttr) {
                    // Build the required name format, e.g., "variants[0].color"
                    input.name = `variants[${index}].${nameAttr}`;
                }
            });
        });
    };

    // Initial re-indexing for rows that already exist on page load (in edit mode).
    reindexRows();

    // --- Event Listener for "Add Variant" button ---
    if (addVariantBtn) {
        addVariantBtn.addEventListener('click', function () {
            // HTML template for a new variant row.
            const newRowHtml = `
                <tr>
                    <td class="text-center align-middle">
                        <input class="form-check-input row-checkbox" type="checkbox">
                    </td>
                    <input type="hidden" data-name="productVariantId" value="">
                    <td><input type="text" data-name="color" class="form-control" required maxlength="50"></td>
                    <td><input type="text" data-name="size" class="form-control" placeholder="e.g., XS,S,M" required maxlength="20"></td>
                    <td><input type="number" step="0.01" min="0" data-name="price" class="form-control"></td>
                    <td><input type="text" data-name="currency" class="form-control" value="VND" maxlength="3"></td>
                    <td class="text-center align-middle">
                        <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn">
                            <i class="bi bi-trash"></i>
                        </button>
                    </td>
                </tr>
            `;
            if(tableBody) {
                // Insert the new row at the end of the table and re-index all rows.
                tableBody.insertAdjacentHTML('beforeend', newRowHtml);
                reindexRows();
            }
        });
    }

    // --- Event Listener for "Delete Selected" button ---
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            if (!tableBody) return;
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No variants selected',
                    text: 'Please select at least one variant to delete.'
                });
                return;
            }
            // Remove each selected row from the DOM.
            checkedBoxes.forEach(checkbox => {
                checkbox.closest('tr').remove();
            });
            // Re-index after deletion to maintain correct sequence.
            reindexRows();
        });
    }

    // --- Event Listener for individual row delete buttons ---
    if (tableBody) {
        tableBody.addEventListener('click', function (e) {
            const deleteButton = e.target.closest('.delete-row-btn');
            if (deleteButton) {
                deleteButton.closest('tr').remove();
                reindexRows();
            }
        });
    }

    // --- Event Listener for "Select All" checkbox ---
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            if (!tableBody) return;
            const checkboxesInBody = tableBody.querySelectorAll('.row-checkbox');
            checkboxesInBody.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    document.addEventListener('input', function(event) {

        const isPriceInput = event.target.matches('input[data-name="price"]')

        if (isPriceInput) {
            const input = event.target;

            input.value = input.value.replace(/[^0-9.]/g, '');

            let value = parseFloat(input.value);

            if (value < 0) {
                input.value = '';
            }
        }
    });

    const productForm = document.getElementById('productForm');

    if (productForm) {
        productForm.addEventListener('submit', function(event) {
            const rows = tableBody.querySelectorAll('tr');
            const seenCombinations = new Set();
            let isDuplicateFound = false;

            rows.forEach(row => row.style.backgroundColor = '');

            for (const row of rows) {
                const colorInput = row.querySelector('input[data-name="color"]');
                const sizeInput = row.querySelector('input[data-name="size"]');

                if (colorInput && sizeInput) {
                    const color = colorInput.value.trim().toLowerCase();
                    const sizes = sizeInput.value.split(/\s*,\s*/);

                    for (const size of sizes) {
                        if (size.trim() === '') continue;

                        const combination = `${color}||${size.trim().toLowerCase()}`;

                        if (seenCombinations.has(combination)) {
                            isDuplicateFound = true;
                            row.style.backgroundColor = 'rgba(255, 0, 0, 0.1)';
                            Swal.fire({
                                icon: 'error',
                                title: 'Duplicate Variant',
                                text: `The combination of Color '${colorInput.value}' and Size '${size}' is duplicated.`
                            });
                            break;
                        } else {
                            seenCombinations.add(combination);
                        }
                    }
                }

                if (isDuplicateFound) {
                    break;
                }
            }

            if (isDuplicateFound) {
                event.preventDefault();
            }
        });
    }

});