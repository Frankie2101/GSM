/**
 * @fileoverview This script provides interactivity for the product_list.mustache page.
 * It handles the "select all" checkbox functionality and triggers a confirmation
 * dialog (using SweetAlert2) before submitting the bulk delete form.
 */
document.addEventListener('DOMContentLoaded', function() {
    // --- Element Selectors ---
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const deleteForm = document.getElementById('deleteForm');
    const deleteBtn = document.getElementById('deleteBtn');

    // --- Event Listener for "Select All" checkbox ---
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            // Set the state of all row checkboxes to match the "select all" checkbox.
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    // --- Event Listener for the main "Delete" button ---
    if (deleteBtn && deleteForm) {
        deleteBtn.addEventListener('click', function() {
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
            const count = checkedBoxes.length;

            // 1. Validate if at least one product is selected.
            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Products Selected',
                    text: 'Please select at least one product to delete.',
                    confirmButtonColor: '#384295'
                });
                return; // Stop the function here.
            }

            // 2. Display a confirmation dialog to the user.
            Swal.fire({
                title: `Delete ${count} Product?`,
                text: `This action is cannot be undone.`,
                icon: null,
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#5a6a85',
                confirmButtonText: 'Delete',
                reverseButtons: true,
                customClass: {
                    container: 'swal2-container-custom'
                }
            }).then((result) => {
                // 3. If the user confirms, submit the delete form.
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});