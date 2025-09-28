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
            document.querySelectorAll('.row-checkbox:not(:disabled)').forEach(checkbox => {
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
                title: `Delete Order(s)?`,
                text: `This action cannot be undone.`,
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

    /**
     * Event listener to provide feedback when a user clicks on a disabled checkbox.
     *
     * This uses event delegation on the table body to efficiently handle clicks. If a click
     * occurs inside a `<span>` with the `disabled-checkbox-wrapper` class, it prevents
     * any default action and displays a temporary "toast" notification (using SweetAlert2)
     * to inform the user why the item cannot be selected for deletion.
     */
    const tableBody = document.querySelector('tbody');
    if (tableBody) {
        tableBody.addEventListener('click', function(event) {
            const wrapper = event.target.closest('.disabled-checkbox-wrapper');
            if (wrapper) {
                event.preventDefault();
                event.stopPropagation();
                Swal.fire({
                    toast: true,
                    position: 'top-end',
                    icon: 'error',
                    title: 'Cannot delete: Order is in use.',
                    showConfirmButton: false,
                    timer: 3500,
                    timerProgressBar: true
                });
            }
        });
    }
});
