/**
 * @fileoverview This script handles the "Export to PDF" functionality on the
 * purchase order print preview page, using the html2canvas and jsPDF libraries.
 */
document.addEventListener('DOMContentLoaded', function () {
    // --- 1. INITIALIZATION & ELEMENT SELECTORS ---
    const exportBtn = document.getElementById('exportPdfBtn');
    if (!exportBtn) return;

    /**
     * Event listener for the "Export to PDF" button.
     */
    function handleExport() {
        const { jsPDF } = window.jspdf;
        const printArea = document.getElementById('poPrintArea');

        const poNumberElement = document.getElementById('poNumberForFileName');
        const poNumber = poNumberElement ? poNumberElement.textContent.trim() : 'UnknownPO';
        const fileName = `PO_${poNumber}.pdf`;

        Swal.fire({
            title: 'Generating PDF...',
            text: 'Please wait.',
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        // Use html2canvas to capture the designated print area as an image.
        html2canvas(printArea, {
            scale: 2,
            useCORS: true,
            windowWidth: printArea.scrollWidth,
            windowHeight: printArea.scrollHeight
        }).then(canvas => {
            // Once captured, get the image data and dimensions.
            const imgData = canvas.toDataURL('image/png');
            const pdf = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' });
            const pdfWidth = pdf.internal.pageSize.getWidth();
            const margin = 20;
            const imgWidth = pdfWidth - (margin * 2);
            const imgHeight = canvas.height * imgWidth / canvas.width;
            let heightLeft = imgHeight;
            let position = margin;

            // Add the image to the first page.
            pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight);
            heightLeft -= pdf.internal.pageSize.getHeight();

            // If the image is taller than one page, loop and add new pages.
            while (heightLeft > 0) {
                position = heightLeft - imgHeight + margin;
                pdf.addPage();
                pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight);
                heightLeft -= pdf.internal.pageSize.getHeight();
            }

            const pdfBlob = pdf.output('blob');
            const url = URL.createObjectURL(pdfBlob);
            const a = document.createElement('a');
            a.href = url;
            a.download = fileName;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            URL.revokeObjectURL(url);

            Swal.close();
        }).catch(err => {
            console.error("Error generating PDF:", err);
            Swal.fire('Error', 'Could not generate PDF.', 'error');
        });
    }

    exportBtn.addEventListener('click', handleExport);
});