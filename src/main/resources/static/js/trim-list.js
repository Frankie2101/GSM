/**
 * @fileoverview Provides interactivity for the trim_list.mustache page.
 * Handles the "select all" checkbox and the delete confirmation dialog.
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
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // --- Event Listener for the main "Delete" button ---
    if (deleteBtn && deleteForm) {
        deleteBtn.addEventListener('click', function() {
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
            const count = checkedBoxes.length;

            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Trims Selected',
                    text: 'Please select at least one trim to delete.',
                    confirmButtonColor: '#384295'
                });
                return;
            }

            // Show confirmation dialog before submitting the form.
            Swal.fire({
                title: `Delete ${count} Trim(s)?`,
                text: `This action cannot be undone.`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#5a6a85',
                confirmButtonText: 'Delete',
                reverseButtons: true
            }).then((result) => {
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});
