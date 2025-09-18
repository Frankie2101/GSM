/**
 * @fileoverview Manages the dynamic and interactive behavior of the fabric_form.mustache page.
 * This script handles adding/deleting fabric color rows and re-indexing input names
 * to ensure correct data binding with Spring MVC.
 */
document.addEventListener('DOMContentLoaded', function () {
    // --- Element Selectors ---
    // Caching references to DOM elements for performance and readability.
    const addBtn = document.getElementById('add-color-btn');
    const deleteSelectedBtn = document.getElementById('delete-selected-btn');
    const tableBody = document.getElementById('fabricColorTableBody');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    /**
     * Re-indexes the `name` attributes of all input fields within the fabric colors table.
     * This is a critical function to ensure that Spring MVC can correctly bind the submitted
     * form data to a List<FabricColorDto>. The names must follow the format `fabricColors[index].fieldName`.
     */
    const reindexRows = () => {
        if (!tableBody) return; // Safety check
        const rows = tableBody.querySelectorAll('tr');

        rows.forEach((row, index) => {
            const inputs = row.querySelectorAll('input');
            inputs.forEach(input => {
                const dataName = input.getAttribute('data-name');
                if (dataName) {
                    // Dynamically constructs the name attribute, e.g., "fabricColors[0].color".
                    input.name = `fabricColors[${index}].${dataName}`;
                }
            });
        });
    };

    // --- USER ACTION HANDLERS ---

    /**
     * Event listener for the "New Detail" button.
     * It dynamically creates a new HTML row and appends it to the table body.
     */
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
                reindexRows(); // CRITICAL: Re-index rows after adding a new one
            }
        });
    }

    /**
     * Event listener for the "Delete" button (for selected rows).
     * It finds all checked checkboxes and removes their corresponding table rows.
     */
    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function () {
            if (!tableBody) return;
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({ icon: 'error', title: 'No Colors Selected', text: 'Please select at least one color to delete.' });
                return;
            }
            checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
            reindexRows(); // CRITICAL: Re-index rows after deletion.
        });
    }

    /**
     * Event listener for individual delete buttons on each row.
     * Uses event delegation to handle clicks on buttons that may be added dynamically.
     */
    if (tableBody) {
        tableBody.addEventListener('click', function (e) {
            // Check if the clicked element or its parent is a delete button.
            if (e.target.closest('.delete-row-btn')) {
                e.target.closest('tr').remove();
                reindexRows(); // CRITICAL: Re-index rows after deletion.
            }
        });
    }

    /**
     * Event listener for the "Select All" checkbox in the table header.
     */
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            if (!tableBody) return;
            tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // Initial re-indexing on page load to set names for pre-existing rows (in edit mode).
    reindexRows();
});