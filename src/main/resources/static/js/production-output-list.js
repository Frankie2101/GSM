document.addEventListener('DOMContentLoaded', function () {
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');
    const deleteBtn = document.getElementById('deleteBtn');
    const deleteForm = document.getElementById('deleteForm');

    // Logic cho "Select All" checkbox
    if(selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function () {
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // Logic cho nút Delete
    if(deleteBtn) {
        deleteBtn.addEventListener('click', function () {
            const selectedIds = Array.from(rowCheckboxes)
                .filter(i => i.checked)
                .map(i => i.value);

            if (selectedIds.length === 0) {
                Swal.fire({
                    icon: 'warning',
                    title: 'No Selection',
                    text: 'Please select at least one item to delete.',
                });
                return;
            }

            Swal.fire({
                title: 'Are you sure?',
                text: `You are about to delete ${selectedIds.length} item(s). You won't be able to revert this!`,
                icon: 'warning',
                showCancelButton: true,
                confirmButtonColor: '#3085d6',
                cancelButtonColor: '#d33',
                confirmButtonText: 'Yes, delete it!'
            }).then((result) => {
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});