/**
 * @fileoverview A client-side application to manage the entire Purchase Order form UI.
 * This script handles data hydration, dynamic UI rendering, cascading dropdowns with lazy loading,
 * real-time calculations, and all API interactions for saving and state changes.
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- 1. INITIALIZATION: Get elements and initial data from the JSON data island ---    const poData = JSON.parse(document.getElementById('po-data').textContent);
    console.log("Loaded PO Data from Server:", poData);

    // ... element selectors ..
    const poForm = document.getElementById('poForm');
    const poNumberSpan = document.getElementById('poNumberSpan');
    const idInput = document.getElementById('purchaseOrderId');
    const supplierSelect = document.getElementById('supplierId');
    const poNoInput = document.getElementById('purchaseOrderNo');
    const poDateInput = document.getElementById('poDate');
    const currencyInput = document.getElementById('currencyCode');
    const deliveryTermInput = document.getElementById('deliveryTerm');
    const paymentTermInput = document.getElementById('paymentTerm');
    const statusInput = document.getElementById('status');
    const totalAmountInput = document.getElementById('totalAmount');

    // Table elements
    const tableBody = document.getElementById('poDetailsTableBody');
    const selectAllDetailsCheckbox = document.getElementById('selectAllDetailsCheckbox');

    // Buttons
    const saveBtn = document.getElementById('savePoBtn');
    const submitBtn = document.getElementById('submitPoBtn');
    const addDetailBtn = document.getElementById('addDetailBtn');
    const deleteSelectedDetailsBtn = document.getElementById('deleteSelectedDetailsBtn');
    const printBtn = document.getElementById('printPoBtn');

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    // --- 2. API HELPERS ---
    async function fetchApi(url, options = {}) {
        const defaultOptions = {
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        };
        const response = await fetch(url, { ...defaultOptions, ...options });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'API Error');
        }
        if (response.status === 204 || (response.status === 200 && options.method === 'DELETE')) return null;
        return response.json();
    }

    /** Fetches the list of all suppliers for the main dropdown. */
    async function loadSuppliers() {
        try {
            const suppliers = await fetchApi('/api/suppliers');
            supplierSelect.innerHTML = '<option value="">-- Select Supplier --</option>';
            suppliers.forEach(s => {
                const option = new Option(s.supplierName, s.supplierId);
                option.dataset.currency = s.currencyCode;
                supplierSelect.add(option);
            });
            if (poData.supplierId) {
                supplierSelect.value = poData.supplierId;
            }
        } catch (e) {
            console.error("Failed to load suppliers", e);
            supplierSelect.innerHTML = '<option value="">Could not load suppliers</option>';
        }
    }

    /** A reusable number formatter for displaying currency values. */
    const numberFormatter = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

    /** Creates and appends a single, fully functional detail row to the table. */
    function createAndAppendRow(detail = {}) {
        const row = tableBody.insertRow();
        row.dataset.detailId = detail.purchaseOrderDetailId || '';
        row.dataset.orderBomDetailId = detail.orderBOMDetailId || ''; // <-- THÊM DÒNG NÀY
        const materialType = detail.materialType || 'FA';
        const isNewRow = !detail.purchaseOrderDetailId;
        const selectedMaterialId = detail.fabricId || detail.trimId || '';

        row.innerHTML = `
            <td><input class="form-check-input row-checkbox" type="checkbox"></td>
            <td>
                <select class="form-select form-select-sm material-type-select">
                    <option value="FA" ${materialType === 'FA' ? 'selected' : ''}>Fabric</option>
                    <option value="TR" ${materialType === 'TR' ? 'selected' : ''}>Trim</option>
                </select>
            </td>
            <td>
                <select class="form-select form-select-sm material-code-select" data-selected-id="${selectedMaterialId}">
                    ${!isNewRow && detail.materialCode ? `<option value="${selectedMaterialId}">${detail.materialCode}</option>` : ''}
                </select>
            </td>
            <td><input type="text" class="form-control form-control-sm material-name-input" value="${detail.materialName || ''}" readonly></td>
            <td>
                <select class="form-select form-select-sm color-select" data-selected-code="${detail.colorCode || ''}">
                     ${!isNewRow && detail.colorCode ? `<option value="${detail.colorCode}">${detail.colorCode}</option>` : ''}
                </select>
            </td>
            <td>
                <select class="form-select form-select-sm size-select" data-selected-code="${detail.size || ''}">
                    ${!isNewRow && detail.size ? `<option value="${detail.size}">${detail.size}</option>` : ''}
                </select>
            </td>
            <td><input type="text" class="form-control form-control-sm uom-input" value="${detail.uom || ''}" readonly></td>
            <td><input type="text" class="form-control form-control-sm text-end qty-input" value="${numberFormatter.format(detail.purchaseQuantity || 0)}" inputmode="decimal"></td>
            <td><input type="text" class="form-control form-control-sm text-end price-input" value="${numberFormatter.format(detail.netPrice || 0)}" inputmode="decimal"></td>
            <td><input type="text" class="form-control form-control-sm text-end tax-rate-input" value="${detail.taxRate || 0}" inputmode="decimal"></td>
            <td><input type="text" class="form-control form-control-sm text-end received-qty-input" value="${numberFormatter.format(detail.receivedQuantity || 0)}" inputmode="decimal"></td>
            <td><input type="text" class="form-control form-control-sm text-end line-amount" readonly></td>
            <td class="text-center"><button type="button" class="btn btn-sm btn-outline-danger delete-row-btn"><i class="bi bi-trash"></i></button></td>
        `;

        if (isNewRow) {
            const typeSelect = row.querySelector('.material-type-select');
            handleMaterialTypeChange(typeSelect);
        }
    }

    /** Renders the initial state of the details table from a data array. */
    function renderDetailsTable(details = []) {
        tableBody.innerHTML = '';
        if (details && details.length > 0) {
            details.forEach(detail => createAndAppendRow(detail));
        }
        updateAllLineAmounts();
    }

    /** Lazily populates a dropdown with data from an API call, only runs once per dropdown. */
    async function populateDropdownIfNeeded(selectElement, apiCall) {
        if (selectElement.dataset.loaded === 'true' && !apiCall) return;

        const selectedValue = selectElement.dataset.selectedId || selectElement.dataset.selectedCode;
        selectElement.dataset.loaded = 'true';

        try {
            const items = await apiCall();
            selectElement.innerHTML = `<option value="">-- Select --</option>`;

            items.forEach(item => {
                const value = item.id || item.code || item.size;
                const text = item.code || item.name || item.size;
                const option = new Option(text, value);

                if (item.price !== undefined) option.dataset.price = item.price;
                if (item.taxRate !== undefined) option.dataset.taxRate = item.taxRate;

                selectElement.add(option);
            });

            if (selectedValue) {
                selectElement.value = selectedValue;
            }
        } catch (e) {
            console.error("Failed to populate dropdown", e);
        }
    }

    /** The series of cascading logic functions. */
    async function handleMaterialTypeChange(typeSelect) {
        const row = typeSelect.closest('tr');
        const codeSelect = row.querySelector('.material-code-select');
        codeSelect.dataset.loaded = 'false'; // Reset to allow reloading
        await populateDropdownIfNeeded(codeSelect, () => fetchApi(`/api/materials?type=${typeSelect.value}`));
        await handleMaterialCodeChange(codeSelect);
    }

    async function handleMaterialCodeChange(codeSelect) {
        const row = codeSelect.closest('tr');
        const materialId = codeSelect.value;
        const type = row.querySelector('.material-type-select').value;
        const colorSelect = row.querySelector('.color-select');

        // Reset fields
        row.querySelector('.material-name-input').value = '';
        row.querySelector('.uom-input').value = '';
        colorSelect.innerHTML = '';
        row.querySelector('.size-select').innerHTML = '';

        if (!materialId) {
            updateAllLineAmounts();
            return;
        }

        const details = await fetchApi(`/api/material-details/${materialId}?type=${type}`);
        row.querySelector('.material-name-input').value = details.name;
        row.querySelector('.uom-input').value = details.unitName;

        colorSelect.dataset.loaded = 'false';
        await populateDropdownIfNeeded(colorSelect, () => fetchApi(`/api/material-colors?type=${type}&materialId=${materialId}`));
        await handleColorChange(colorSelect);
    }

    async function handleColorChange(colorSelect) {
        const row = colorSelect.closest('tr');
        const colorCode = colorSelect.value;
        const type = row.querySelector('.material-type-select').value;
        const materialId = row.querySelector('.material-code-select').value;
        const sizeSelect = row.querySelector('.size-select');

        sizeSelect.innerHTML = '';

        if (!colorCode || type !== 'TR' || !materialId) return;

        sizeSelect.dataset.loaded = 'false';
        await populateDropdownIfNeeded(sizeSelect, () => fetchApi(`/api/material-sizes?trimId=${materialId}&colorCode=${colorCode}`));
    }

    /** Calculates the total amount for a single row. */
    function updateLineAmount(row) {
        const qty = parseFloat(row.querySelector('.qty-input').value.replace(/,/g, '')) || 0;
        const price = parseFloat(row.querySelector('.price-input').value.replace(/,/g, '')) || 0;
        const taxRate = parseFloat(row.querySelector('.tax-rate-input').value.replace(/,/g, '')) || 0;
        const amount = qty * price * (1 + taxRate / 100.0);
        row.querySelector('.line-amount').value = numberFormatter.format(amount);
    }

    /** Recalculates all line amounts and the grand total. */
    function updateAllLineAmounts() {
        tableBody.querySelectorAll('tr').forEach(updateLineAmount);
        updateTotalAmount();
    }

    /** Calculates the grand total of the entire PO. */
    function updateTotalAmount() {
        let total = 0;
        tableBody.querySelectorAll('.line-amount').forEach(input => {
            total += parseFloat(input.value.replace(/,/g, '')) || 0;
        });
        totalAmountInput.value = numberFormatter.format(total);
    }

    /** Updates the price and tax fields based on the selected option in a dropdown. */
    function updatePriceAndTaxFromSelection(selectElement) {
        const row = selectElement.closest('tr');
        const priceInput = row.querySelector('.price-input');
        const taxInput = row.querySelector('.tax-rate-input');

        const selectedOption = selectElement.options[selectElement.selectedIndex];

        if (!selectedOption || !selectedOption.value) {
            priceInput.value = numberFormatter.format(0);
            taxInput.value = 0;
            updateAllLineAmounts();
            return;
        }

        const price = selectedOption.dataset.price || 0;
        const taxRate = selectedOption.dataset.taxRate || 0;

        priceInput.value = numberFormatter.format(parseFloat(price));
        taxInput.value = taxRate;

        updateAllLineAmounts();
    }

    /** Enables or disables all form fields based on the PO status. */
    function setFormReadOnly(readOnly) {
        poForm.querySelectorAll('input, select').forEach(el => {
            if (el.id !== 'status') el.disabled = readOnly;
        });
        const displayStyle = readOnly ? 'none' : 'inline-block';
        addDetailBtn.style.display = displayStyle;
        deleteSelectedDetailsBtn.style.display = displayStyle;
        tableBody.querySelectorAll('.delete-row-btn').forEach(btn => {
            btn.closest('td').style.display = readOnly ? 'none' : 'table-cell';
        });
        tableBody.querySelectorAll('.row-checkbox').forEach(cb => {
            cb.closest('td').style.display = readOnly ? 'none' : 'table-cell';
        });
        selectAllDetailsCheckbox.closest('th').style.display = readOnly ? 'none' : 'table-cell';
    }

    // --- 4. MAIN INITIALIZATION FUNCTION ---
    /**
     * The main function that runs on page load to set up the entire form.
     */
    async function initializeForm() {
        // Step 1: Asynchronously load the suppliers list for the main dropdown.
        await loadSuppliers();
        const isNew = !poData.purchaseOrderId;

        // Step 2: Populate all header fields from the poData object.
        poNumberSpan.textContent = poData.purchaseOrderNo || '(New)';
        idInput.value = poData.purchaseOrderId || '';
        poNoInput.value = poData.purchaseOrderNo || '';
        currencyInput.value = poData.currencyCode || '';
        deliveryTermInput.value = poData.deliveryTerm || '';
        paymentTermInput.value = poData.paymentTerm || '';
        statusInput.value = poData.status || 'New';

        if (poData.poDate) {
            let dateValue = poData.poDate;
            if (Array.isArray(dateValue)) {
                const [year, month, day] = dateValue;
                dateValue = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            }
            poDateInput.value = dateValue.split('T')[0];
        } else if (isNew) {
            poDateInput.value = new Date().toISOString().split('T')[0];
        }

        // Step 3: Render the initial details table using the data from the server.
        renderDetailsTable(poData.details);

        // Step 4: Determine the PO's status and set the form's interactivity.
        const status = statusInput.value;
        if (status === 'New' || status === 'Rejected') {
            // If the PO is editable, enable fields and show relevant buttons.
            setFormReadOnly(false);
            saveBtn.style.display = 'inline-block';
            submitBtn.style.display = 'inline-block';
        } else {
            // If the PO is submitted/approved, lock the form.
            setFormReadOnly(true);
            saveBtn.style.display = 'none';
            submitBtn.style.display = 'none';
        }

        // Step 5: Show the print button only for existing POs.
        if (!isNew) {
            printBtn.style.display = 'inline-block';
        }

        // Step 6: Attach the click event to the Print button.
        printBtn.addEventListener('click', () => {
            const poId = idInput.value;
            if (poId) {
                window.open(`/purchase_orders/print-preview/${poId}`, '_blank');
            }
        });
    }

    // --- 5. EVENT LISTENERS ---

    supplierSelect.addEventListener('change', function() {
        const selectedOption = this.options[this.selectedIndex];
        currencyInput.value = selectedOption.dataset.currency || 'USD';
    });

    tableBody.addEventListener('change', async function(e) {
        const target = e.target;
        const row = target.closest('tr');

        if (target.classList.contains('material-type-select')) {
            await handleMaterialTypeChange(target);
        } else if (target.classList.contains('material-code-select')) {
            await handleMaterialCodeChange(target);
        } else if (target.classList.contains('color-select')) {
            await handleColorChange(target);
            const type = row.querySelector('.material-type-select').value;
            if (type === 'FA') {
                updatePriceAndTaxFromSelection(target);
            }
        } else if (target.classList.contains('size-select')) {
            updatePriceAndTaxFromSelection(target);
        }
    });

    tableBody.addEventListener('focus', async (e) => {
        const target = e.target;
        if (target.matches('.material-code-select:not([data-loaded]), .color-select:not([data-loaded]), .size-select:not([data-loaded])')) {
            await populateDropdownIfNeeded(target, async () => {
                const row = target.closest('tr');
                const type = row.querySelector('.material-type-select').value;
                const materialId = row.querySelector('.material-code-select').value;
                if (target.classList.contains('material-code-select')) {
                    return fetchApi(`/api/materials?type=${type}`);
                }
                if (target.classList.contains('color-select') && materialId) {
                    return fetchApi(`/api/material-colors?type=${type}&materialId=${materialId}`);
                }
                if (target.classList.contains('size-select') && materialId && type === 'TR') {
                    const colorCode = row.querySelector('.color-select').value;
                    if (colorCode) {
                        return fetchApi(`/api/material-sizes?trimId=${materialId}&colorCode=${colorCode}`);
                    }
                }
                return [];
            });
        }
    }, true);

    tableBody.addEventListener('click', function(e) {
        if (e.target.closest('.delete-row-btn')) {
            e.target.closest('tr').remove();
            updateAllLineAmounts();
        }
    });

    tableBody.addEventListener('input', e => {
        const targetClassList = e.target.classList;
        if (targetClassList.contains('qty-input') ||
            targetClassList.contains('price-input') ||
            targetClassList.contains('tax-rate-input')) {
            updateAllLineAmounts();
        }
    });

    tableBody.addEventListener('focusin', e => {
        const target = e.target;
        if (target.matches('.qty-input, .price-input, .received-qty-input, .tax-rate-input')) {
            if (target.value) target.value = target.value.replace(/,/g, '');
        }
    });

    tableBody.addEventListener('focusout', e => {
        const target = e.target;
        if (target.matches('.qty-input, .price-input, .received-qty-input')) {
            if (target.value) {
                const numericValue = parseFloat(target.value.replace(/,/g, ''));
                target.value = numberFormatter.format(isNaN(numericValue) ? 0 : numericValue);
            }
        }
    });

    addDetailBtn.addEventListener('click', () => createAndAppendRow());

    deleteSelectedDetailsBtn.addEventListener('click', function() {
        const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
        if (checkedBoxes.length === 0) {
            Swal.fire('No selection', 'Please select details to delete.', 'warning');
            return;
        }
        Swal.fire({
            title: 'Are you sure?',
            text: `Delete ${checkedBoxes.length} selected row(s)?`,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            confirmButtonText: 'Yes, delete them!'
        }).then((result) => {
            if (result.isConfirmed) {
                checkedBoxes.forEach(box => box.closest('tr').remove());
                updateAllLineAmounts();
            }
        });
    });

    selectAllDetailsCheckbox.addEventListener('change', function() {
        tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
            checkbox.checked = this.checked;
        });
    });

    /**
     * Listener for the "Save" button.
     */
    saveBtn.addEventListener('click', async () => {
             // 1. Collect all data from the detail rows into a structured array.
            const details = Array.from(tableBody.rows).map(row => {
            const materialType = row.querySelector('.material-type-select').value;
            const materialId = parseInt(row.querySelector('.material-code-select').value);
            const qty = parseFloat(row.querySelector('.qty-input').value.replace(/,/g, '')) || 0;
            const price = parseFloat(row.querySelector('.price-input').value.replace(/,/g, '')) || 0;
            const taxRate = parseFloat(row.querySelector('.tax-rate-input').value.replace(/,/g, '')) || 0;
            const receivedQuantity = parseFloat(row.querySelector('.received-qty-input').value.replace(/,/g, '')) || 0;

            return {
                purchaseOrderDetailId: row.dataset.detailId ? parseInt(row.dataset.detailId) : null,
                orderBOMDetailId: row.dataset.orderBomDetailId ? parseInt(row.dataset.orderBomDetailId) : null, // <-- THÊM DÒNG NÀY
                fabricId: materialType === 'FA' ? materialId : null,
                trimId: materialType === 'TR' ? materialId : null,
                purchaseQuantity: qty,
                netPrice: price,
                taxRate: taxRate,
                receivedQuantity: receivedQuantity,
            };
        });

        // 2. Collect all data from the header fields.
        const payload = {
            purchaseOrderId: idInput.value ? parseInt(idInput.value) : null,
            purchaseOrderNo: poNoInput.value,
            poDate: poDateInput.value,
            supplierId: parseInt(supplierSelect.value),
            currencyCode: currencyInput.value,
            deliveryTerm: deliveryTermInput.value,
            paymentTerm: paymentTermInput.value,
            status: statusInput.value,
            details: details
        };

        // 3. Submit the complete payload to the save API.
        try {
            const savedPo = await fetchApi('/api/purchase_orders', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            // 4. On success, show a message and reload to the edit page.
            Swal.fire('Success!', 'Purchase Order saved successfully.', 'success').then(() => {
                window.location.href = `/purchase_orders/form?id=${savedPo.purchaseOrderId}`;
            });
        } catch (e) {
            // 5. On failure, show an error message.
            Swal.fire('Error!', `Failed to save Purchase Order: ${e.message}`, 'error');
        }
    });

    /**
     * Listener for the "Submit for Approval" button.
     */
    submitBtn.addEventListener('click', () => {
        const poId = idInput.value;
        if (!poId) {
            Swal.fire('Warning', 'Please save the Purchase Order before submitting.', 'warning');
            return;
        }

        Swal.fire({
            title: 'Submit for Approval?',
            text: 'Once submitted, you cannot edit this PO unless it is rejected.',
            icon: 'info',
            showCancelButton: true,
            confirmButtonText: 'Yes, submit it!'
        }).then(async (result) => {
            if (result.isConfirmed) {
                try {
                    await fetchApi(`/api/purchase_orders/${poId}/submit`, { method: 'POST' });
                    Swal.fire('Submitted!', 'The PO has been submitted for approval.', 'success')
                        .then(() => window.location.reload());
                } catch (e) {
                    Swal.fire('Error!', `Could not submit the PO. ${e.message}`, 'error');
                }
            }
        });
    });

    // --- 6. RUN INITIALIZATION ---
    initializeForm();
});

