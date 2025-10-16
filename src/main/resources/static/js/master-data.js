/**
 * @fileoverview Manages the two-step import process (preview and confirm)
 * for all master data types on the master data page.
 */
document.addEventListener('DOMContentLoaded', function () {
    let currentDataType = '';

    /**
     * Handles the initial file upload for preview.
     * @param {string} formId The ID of the upload form.
     * @param {string} dataType The identifier for the data type (e.g., 'customers').
     */
    const handleUploadForPreview = (formId, dataType) => {
        const form = document.getElementById(formId);
        if (!form) return;

        const messageDiv = form.querySelector('.mt-3');
        const submitButton = form.querySelector('button[type="submit"]');

        form.addEventListener('submit', async function (event) {
            event.preventDefault();

            const csrfInput = form.querySelector('input[name="_csrf"]');
            const csrfToken = csrfInput ? csrfInput.value : null;
            const csrfHeaderName = "X-CSRF-TOKEN";

            const fileInput = form.querySelector('input[type="file"]');
            const file = fileInput.files[0];
            if (!file) {
                messageDiv.className = 'alert alert-danger p-2';
                messageDiv.textContent = 'Please select a file.';
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            submitButton.disabled = true;
            submitButton.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Uploading...`;
            messageDiv.textContent = '';

            try {
                const headers = new Headers();
                if (csrfToken) {
                    headers.append(csrfHeaderName, csrfToken);
                }

                const response = await fetch(`/api/import/preview/${dataType}`, {
                    method: 'POST',
                    headers: headers,
                    body: formData
                });

                const result = await response.json();

                if (!response.ok) {
                    throw new Error(result.message || 'Error parsing file.');
                }

                document.getElementById('cacheKeyInput').value = result.cacheKey;
                currentDataType = dataType;

                populatePreviewTable(result.data, dataType);

                const importModal = bootstrap.Modal.getInstance(form.closest('.modal'));
                const previewModal = bootstrap.Modal.getOrCreateInstance(document.getElementById('previewModal'));

                if (importModal) importModal.hide();
                previewModal.show();

            } catch (error) {
                messageDiv.className = 'alert alert-danger p-2';
                messageDiv.textContent = error.message;
            } finally {
                submitButton.disabled = false;
                submitButton.innerHTML = 'Upload';
                form.reset();
            }
        });
    };

    /**
     * Dynamically generates and populates the preview table based on the data type.
     * @param {Array<Object>} data An array of objects to display.
     * @param {string} dataType The current data type (e.g., 'customers').
     */
    function populatePreviewTable(data, dataType) {
        const tableHead = document.getElementById('previewTableHead');
        const tableBody = document.getElementById('previewTableBody');
        tableHead.innerHTML = '';
        tableBody.innerHTML = '';

        const columnConfig = {
            customers: ['customerCode', 'customerName', 'address', 'contactPhone', 'contactEmail', 'deliveryTerm', 'paymentTerm', 'currencyCode', 'countryCode'],
            suppliers: ['supplierCode', 'supplierName', 'address', 'contactPhone', 'contactEmail', 'paymentTerm', 'countryCode', 'taxRate'],
            units: ['unitCode', 'unitName']
        };

        if (!data || !Array.isArray(data) || data.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="100%" class="text-center text-muted">No data to preview. The file might be empty.</td></tr>';
            return;
        }

        const toTitleCase = (text) => {
            const result = text.replace(/([A-Z])/g, ' $1');
            return result.charAt(0).toUpperCase() + result.slice(1);
        };

        const headersToShow = columnConfig[dataType] || Object.keys(data[0]);
        let headerHtml = '<tr>';

        headersToShow.forEach(header => {
            const title = toTitleCase(header);
            const lowerHeader = header.toLowerCase();

            if (lowerHeader.endsWith('code')) {
                headerHtml += `<th class="th-code">${title}</th>`;
            } else if (lowerHeader.endsWith('name')) {
                headerHtml += `<th class="th-name">${title}</th>`;
            } else if (lowerHeader === 'address') {
                headerHtml += `<th class="th-address">${title}</th>`;
            } else {
                headerHtml += `<th>${title}</th>`;
            }
        });
        headerHtml += '</tr>';

        tableHead.className = 'preview-table-header';
        tableHead.innerHTML = headerHtml;

        tableBody.innerHTML = data.map(row =>
            `<tr>${headersToShow.map(header => `<td>${row[header] || ''}</td>`).join('')}</tr>`
        ).join('');
    }

    /**
     * Handles the final confirmation step.
     */
    document.getElementById('confirmImportBtn').addEventListener('click', async function() {
        const button = this;
        const cacheKey = document.getElementById('cacheKeyInput').value;
        const csrfInput = document.querySelector('#customerImportForm input[name="_csrf"]'); // Lấy từ một form bất kỳ
        const csrfToken = csrfInput ? csrfInput.value : null;
        const csrfHeaderName = "X-CSRF-TOKEN";

        button.disabled = true;
        button.innerHTML = `<span class="spinner-border spinner-border-sm"></span> Confirming...`;

        try {
            const headers = { 'Content-Type': 'application/json' };
            if (csrfToken) {
                headers[csrfHeaderName] = csrfToken;
            }

            const response = await fetch(`/api/import/confirm/${currentDataType}`, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify({ cacheKey: cacheKey })
            });
            const resultText = await response.text();
            if (!response.ok) throw new Error(resultText);

            const previewModal = bootstrap.Modal.getInstance(document.getElementById('previewModal'));
            if(previewModal) previewModal.hide();

            Swal.fire({ icon: 'success', title: 'Import Successful!', timer: 1500, showConfirmButton: false })
                .then(() => location.reload());

        } catch (error) {
            Swal.fire({ icon: 'error', title: 'Import Failed', text: error.message });
        } finally {
            button.disabled = false;
            button.innerHTML = 'Confirm Import';
        }
    });

// Initialize handlers.
handleUploadForPreview('customerImportForm', 'customers');
handleUploadForPreview('supplierImportForm', 'suppliers');
handleUploadForPreview('unitImportForm', 'units');
});