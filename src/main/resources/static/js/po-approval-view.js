/**
 * @fileoverview Handles user actions (Approve, Reject) on the PO approval view page.
 */
document.addEventListener('DOMContentLoaded', function () {
    // --- 1. INITIALIZATION & ELEMENT SELECTORS ---
    const approveBtn = document.querySelector('.approve-btn');
    const rejectBtn = document.querySelector('.reject-btn');
    const printBtn = document.getElementById('printBtn');
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');


    if (!approveBtn || !rejectBtn) return;

    // Get the PO ID from the button's data attribute.
    const poId = approveBtn.dataset.poId;

    // --- 2. API HELPERS ---
    /**
     * A generic fetch wrapper.
     */
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

    // --- 3. CORE LOGIC ---
    /**
     * Handles both the 'approve' and 'reject' actions.
     * @param {string} action - The action to perform ('approve' or 'reject').
     */
    async function handleApproval(action) {
        // Step 1: Determine texts and colors for the confirmation dialog based on the action.
        const isApproving = action === 'approve';
        // Step 2: Show a confirmation dialog to the user.
        const result = await Swal.fire({
            title: `${isApproving ? 'Approve' : 'Reject'} Purchase Order?`,
            text: `This action cannot be undone.`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: isApproving ? '#198754' : '#dc3545',
            confirmButtonText: `${action}`
        });

        // Step 3: If the user confirmed the action...
        if (result.isConfirmed) {
            try {
                // Call the specific API endpoint for the action (e.g., /api/purchase_orders/123/approve).
                await fetchApi(`/api/purchase_orders/${poId}/${action}`, { method: 'POST' });
                // Show a success message.
                await Swal.fire(
                    isApproving ? 'Approved!' : 'Rejected!',
                    `The PO has been successfully ${action}d.`,
                    'success'
                );
                // Redirect the user back to the pending list.
                window.location.href = '/purchase_orders/pending-approval';
            } catch (error) {
            }
        }
    }

    // --- 4. EVENT LISTENERS ---
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
            scale: 2,
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
            const imgWidth = pdfWidth - 40;
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

    // --- 4. EVENT LISTENERS ---
    approveBtn.addEventListener('click', () => handleApproval('approve'));
    rejectBtn.addEventListener('click', () => handleApproval('reject'));
    if (printBtn) printBtn.addEventListener('click', handlePrint);
});