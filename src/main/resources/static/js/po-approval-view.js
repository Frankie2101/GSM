document.addEventListener('DOMContentLoaded', function () {
    const approveBtn = document.querySelector('.approve-btn');
    const rejectBtn = document.querySelector('.reject-btn');
    const printBtn = document.getElementById('printBtn'); // Nút Print mới


    if (!approveBtn || !rejectBtn) return;

    const poId = approveBtn.dataset.poId;

    async function fetchApi(url, options = {}) {
        try {
            const response = await fetch(url, {
                headers: { 'Content-Type': 'application/json' },
                ...options
            });
            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || `API call failed: ${response.status}`);
            }
            return null;
        } catch (error) {
            Swal.fire('API Error', error.message, 'error');
            throw error;
        }
    }

    async function handleApproval(action) {
        const isApproving = action === 'approve';
        const result = await Swal.fire({
            title: `${isApproving ? 'Approve' : 'Reject'} Purchase Order?`,
            text: `This action cannot be undone.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: isApproving ? '#198754' : '#dc3545',
            confirmButtonText: `${action}`
        });

        if (result.isConfirmed) {
            try {
                await fetchApi(`/api/purchase_orders/${poId}/${action}`, { method: 'POST' });

                await Swal.fire(
                    isApproving ? 'Approved!' : 'Rejected!',
                    `The PO has been successfully ${action}d.`,
                    'success'
                );
                // Redirect back to the approval list
                window.location.href = '/purchase_orders/pending-approval';
            } catch (error) {
                // Lỗi đã được hiển thị bởi fetchApi
            }
        }
    }

    // --- LOGIC MỚI CHO CHỨC NĂNG IN PDF ---
    function handlePrint() {
        const { jsPDF } = window.jspdf;
        const printArea = document.getElementById('poPrintArea');
        const poNumber = document.querySelector('.col-6.text-end p:first-child strong').textContent;
        const fileName = `PO_${poNumber.trim()}.pdf`;

        Swal.fire({
            title: 'Generating PDF...',
            text: 'Please wait a moment.',
            allowOutsideClick: false,
            didOpen: () => {
                Swal.showLoading();
            }
        });

        html2canvas(printArea, {
            scale: 2, // Tăng chất lượng hình ảnh
            useCORS: true
        }).then(canvas => {
            const imgData = canvas.toDataURL('image/png');
            const pdf = new jsPDF({
                orientation: 'portrait',
                unit: 'pt',
                format: 'a4'
            });

            const pdfWidth = pdf.internal.pageSize.getWidth();
            const pdfHeight = pdf.internal.pageSize.getHeight();
            const canvasWidth = canvas.width;
            const canvasHeight = canvas.height;
            const ratio = canvasWidth / canvasHeight;

            const imgWidth = pdfWidth - 40; // Trừ lề 20pt mỗi bên
            const imgHeight = imgWidth / ratio;

            let position = 20;

            pdf.addImage(imgData, 'PNG', 20, position, imgWidth, imgHeight);

            pdf.save(fileName);

            Swal.close();
        }).catch(err => {
            console.error("Error generating PDF:", err);
            Swal.fire('Error', 'Could not generate PDF.', 'error');
        });
    }

    approveBtn.addEventListener('click', () => handleApproval('approve'));
    rejectBtn.addEventListener('click', () => handleApproval('reject'));
    if (printBtn) printBtn.addEventListener('click', handlePrint);
});