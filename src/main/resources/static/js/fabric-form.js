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
                    <td><input type="text" data-name="color" class="form-control" required maxlength="50"></td>
                    <td><input type="text" data-name="colorName" class="form-control" maxlength="100"></td>
                    <td><input type="text" data-name="width" class="form-control" maxlength="50"></td>
                    <td><input type="number" step="0.01" min="0" data-name="netPrice" class="form-control"></td>
                    <td><input type="number" step="0.01" min="0" data-name="taxPercent" class="form-control"></td>
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

    /**
     * Provides real-time validation for numeric input fields to ensure only valid numbers are entered.
     *
     * This listener uses event delegation on the entire document to catch `input` events.
     * It targets fields with `data-name="netPrice"` or `data-name="taxPercent"`.
     * For these fields, it automatically removes any non-numeric characters (except for the decimal point)
     * and clears the input if a negative value is entered.
     */
    document.addEventListener('input', function(event) {
        const isNumericInput = event.target.matches('input[data-name="netPrice"]') ||
            event.target.matches('input[data-name="taxPercent"]');

        if (isNumericInput) {
            const input = event.target;

            input.value = input.value.replace(/[^0-9.]/g, '');

            if (parseFloat(input.value) < 0) {
                input.value = '';
            }
        }
    });


    /**
     * Event listener for the main form's `submit` event to validate for duplicate color codes.
     *
     * Before allowing the form to be submitted to the server, this function iterates through
     * all variant rows in the table. It uses a Set to efficiently check if any `Color Code`
     * values are duplicated (case-insensitive). If a duplicate is found, it prevents the
     * form submission, highlights the problematic row with a light red background,
     * and displays a SweetAlert2 error message to the user.
     */
    const fabricForm = document.getElementById('fabricForm');
    if (fabricForm) {
        fabricForm.addEventListener('submit', function(event) {
            const rows = tableBody.querySelectorAll('tr');
            const seenColorCodes = new Set();
            let isDuplicateFound = false;

            rows.forEach(row => row.style.backgroundColor = '');

            for (const row of rows) {
                const colorInput = row.querySelector('input[data-name="color"]');

                if (colorInput) {
                    const colorCode = colorInput.value.trim().toLowerCase();

                    if (seenColorCodes.has(colorCode)) {
                        isDuplicateFound = true;
                        row.style.backgroundColor = 'rgba(255, 0, 0, 0.1)';
                        Swal.fire({
                            icon: 'error',
                            title: 'Duplicate Color Code',
                            text: `The Color Code '${colorInput.value}' is duplicated.`
                        });
                        break;
                    } else {
                        seenColorCodes.add(colorCode);
                    }
                }
            }

            if (isDuplicateFound) {
                event.preventDefault();
            }
        });
    }

    // Initial re-indexing on page load to set names for pre-existing rows (in edit mode).
    reindexRows();
});