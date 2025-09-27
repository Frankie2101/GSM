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
                title: `Delete Trim(s)?`,
                text: `This action cannot be undone.`,
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
                    title: 'Cannot delete, already existing in BOM',
                    showConfirmButton: false,
                    timer: 3500,
                    timerProgressBar: true
                });
            }
        });
    }
});
