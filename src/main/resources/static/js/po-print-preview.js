document.addEventListener('DOMContentLoaded', function () {
    const exportBtn = document.getElementById('exportPdfBtn');
    if (!exportBtn) return;

    function handleExport() {
        const { jsPDF } = window.jspdf;
        const printArea = document.getElementById('poPrintArea');

        // --- SỬA LẠI LOGIC LẤY TÊN FILE TẠI ĐÂY ---
        const poNumberElement = document.getElementById('poNumberForFileName');
        const poNumber = poNumberElement ? poNumberElement.textContent.trim() : 'UnknownPO';
        const fileName = `PO_${poNumber}.pdf`;
        // ------------------------------------------

        Swal.fire({
            title: 'Generating PDF...',
            text: 'Please wait.',
            allowOutsideClick: false,
            didOpen: () => Swal.showLoading()
        });

        html2canvas(printArea, {
            scale: 2,
            useCORS: true,
            windowWidth: printArea.scrollWidth,
            windowHeight: printArea.scrollHeight
        }).then(canvas => {
            // ... phần logic tạo PDF còn lại giữ nguyên ...
            const imgData = canvas.toDataURL('image/png');
            const pdf = new jsPDF({ orientation: 'portrait', unit: 'pt', format: 'a4' });
            const pdfWidth = pdf.internal.pageSize.getWidth();
            const margin = 20;
            const imgWidth = pdfWidth - (margin * 2);
            const imgHeight = canvas.height * imgWidth / canvas.width;
            let heightLeft = imgHeight;
            let position = margin;

            pdf.addImage(imgData, 'PNG', margin, position, imgWidth, imgHeight);
            heightLeft -= pdf.internal.pageSize.getHeight();

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