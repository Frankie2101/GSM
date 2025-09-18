/**
 * @fileoverview This script provides interactivity for the sale_order_list.mustache page.
 * It handles the "select all" checkbox functionality and triggers a confirmation
 * dialog (using SweetAlert2) before submitting the bulk delete form.
 */
document.addEventListener('DOMContentLoaded', function() {
    // --- Element Selectors ---
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const deleteForm = document.getElementById('deleteForm');
    const deleteBtn = document.getElementById('deleteBtn');

    /**
     * Event listener for the "Select All" checkbox.
     * Toggles the checked state of all individual row checkboxes.
     */
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    /**
     * Event listener for the main "Delete" button.
     * Validates that at least one item is selected, then shows a confirmation dialog.
     */
    if (deleteBtn && deleteForm) {
        deleteBtn.addEventListener('click', function() {
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
            const count = checkedBoxes.length;

            // 1. Validate if at least one order is selected.
            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Orders Selected',
                    text: 'Please select at least one sale order to delete.',
                    confirmButtonColor: '#384295'
                });
                return;
            }

            // 2. Display a confirmation dialog to the user before proceeding.
            Swal.fire({
                title: `Delete ${count} Order(s)?`,
                text: `This action cannot be undone.`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#5a6a85',
                confirmButtonText: 'Delete',
                reverseButtons: true
            }).then((result) => {
                // 3. If the user confirms the action, submit the delete form.
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});
