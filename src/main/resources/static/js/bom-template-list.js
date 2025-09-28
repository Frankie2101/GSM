
/**
 * @fileoverview This script provides interactivity for the bom_template_list.mustache page.
 * It handles the "select all" checkbox functionality and triggers a confirmation
 * dialog (using SweetAlert2) before submitting the bulk delete form.
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- 1. Element Selectors ---
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
            //Count selected items.
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
            const count = checkedBoxes.length;

            //Validate selection.
            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Templates Selected',
                    text: 'Please select at least one template to delete.',
                    confirmButtonColor: '#384295'
                });
                return;
            }

            //Show confirmation dialog.
            Swal.fire({
                title: `Delete Template(s)?`,
                text: `This action cannot be undone.`,
                showCancelButton: true,
                confirmButtonColor: '#d33',
                cancelButtonColor: '#5a6a85',
                confirmButtonText: 'Delete',
                reverseButtons: true
            }).then((result) => {
                //Submit form if confirmed.
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }

    /**
     * Use event delegation to listen for clicks anywhere inside the table body.
     * This is efficient as it only requires one event listener for the entire table.
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
                    title: 'Cannot delete: Template is existing in BOM.',
                    showConfirmButton: false,
                    timer: 3500,
                    timerProgressBar: true
                });
            }
        });
    }
});
