document.addEventListener('DOMContentLoaded', function () {
    const handleImportForm = (formId, apiUrl, messageDivId) => {
        const form = document.getElementById(formId);
        if (!form) return;

        form.addEventListener('submit', async function (event) {
            event.preventDefault();
            const fileInput = form.querySelector('input[type="file"]');
            const messageDiv = document.getElementById(messageDivId);
            const submitButton = form.querySelector('button[type="submit"]');

            const file = fileInput.files[0];
            if (!file) {
                messageDiv.className = 'alert alert-danger';
                messageDiv.textContent = 'Please select a file.';
                return;
            }

            const formData = new FormData();
            formData.append('file', file);

            submitButton.disabled = true;
            submitButton.textContent = 'Uploading...';
            messageDiv.textContent = '';

            try {
                const response = await fetch(apiUrl, { method: 'POST', body: formData });
                const resultText = await response.text();

                if (response.ok) {
                    Swal.fire({ icon: 'success', title: 'Import Successful!', timer: 2000, showConfirmButton: false })
                        .then(() => location.reload());
                } else {
                    messageDiv.className = 'alert alert-danger';
                    messageDiv.textContent = `Error: ${resultText}`;
                }
            } catch (error) {
                messageDiv.className = 'alert alert-danger';
                messageDiv.textContent = 'A network error occurred.';
            } finally {
                submitButton.disabled = false;
                submitButton.textContent = 'Upload';
            }
        });
    };

    // Initialize handlers for all three import forms
    handleImportForm('customerImportForm', '/api/import/customers', 'customerImportMessage');
    handleImportForm('supplierImportForm', '/api/import/suppliers', 'supplierImportMessage');
    handleImportForm('unitImportForm', '/api/import/units', 'unitImportMessage');

    console.log('master-data.js loaded.');
});