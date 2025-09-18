/**
 * @fileoverview Provides interactivity for the fabric_list.mustache page.
 * This script handles the "select all" functionality and shows a confirmation
 * dialog using SweetAlert2 before submitting the bulk delete form.
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
                checkbox.checked = selectAllCheckbox.checked;
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

            // 1. Validate selection.
            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Fabrics Selected',
                    text: 'Please select at least one fabric to delete.',
                    confirmButtonColor: '#384295'
                });
                return;
            }

            // 2. Show confirmation dialog.
            Swal.fire({
                title: `Delete ${count} Fabric(s)?`,
                text: `This action cannot be undone.`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#5a6a85',
                confirmButtonText: 'Delete',
                reverseButtons: true,
                customClass: {
                    container: 'swal2-container-custom'
                }
            }).then((result) => {
                // 3. Submit form if confirmed.
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});
