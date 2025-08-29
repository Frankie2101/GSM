document.addEventListener('DOMContentLoaded', function() {
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const rowCheckboxes = document.querySelectorAll('.row-checkbox');

    // Giữ lại việc khai báo các biến quan trọng ở đây
    const deleteForm = document.getElementById('deleteForm');
    const deleteBtn = document.getElementById('deleteBtn');

    // Xử lý checkbox "Chọn tất cả"
    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            rowCheckboxes.forEach(checkbox => {
                checkbox.checked = selectAllCheckbox.checked;
            });
        });
    }

    // Gán sự kiện click nếu cả hai element đều tồn tại
    if (deleteBtn && deleteForm) {
        deleteBtn.addEventListener('click', function() {
            const checkedBoxes = document.querySelectorAll('.row-checkbox:checked');
            const count = checkedBoxes.length;

            // Xử lý khi chưa chọn sản phẩm
            if (count === 0) {
                Swal.fire({
                    icon: 'error',
                    title: 'No Products Selected',
                    text: 'Please select at least one product to delete.',
                    confirmButtonColor: '#384295'
                });
                return;
            }

            // Hiển thị hộp thoại xác nhận
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
                    container: 'swal2-container-custom' // Thêm class tùy chỉnh cho container
                }
            }).then((result) => {
                if (result.isConfirmed) {
                    deleteForm.submit();
                }
            });
        });
    }
});