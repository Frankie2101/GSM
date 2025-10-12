/**
 * @fileoverview A client-side application to manage the entire Purchase Order form UI.
 * This script handles data hydration, dynamic UI rendering, cascading dropdowns with lazy loading,
 * real-time calculations, and all API interactions for saving and state changes.
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- 1. INITIALIZATION: Get elements and initial data from the JSON data island ---
    const poData = JSON.parse(document.getElementById('po-data').textContent);
    const materialGroupTemplate = document.getElementById('material-groups-data-template');
    console.log("Loaded PO Data from Server:", poData);

    // ... element selectors ..
    const poForm = document.getElementById('poForm');
    const poNumberSpan = document.getElementById('poNumberSpan');
    const idInput = document.getElementById('purchaseOrderId');
    const supplierSelect = document.getElementById('supplierId');
    const poNoInput = document.getElementById('purchaseOrderNo');
    const poDateInput = document.getElementById('poDate');
    const arrivalDateInput = document.getElementById('arrivalDate');
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
    const materialCodeCache = {};
    const colorCache = {};
    const sizeCache = {};

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
    async function createAndAppendRow(detail = {}) {
        const row = tableBody.insertRow();
        row.dataset.detailId = detail.purchaseOrderDetailId || '';
        row.dataset.orderBomDetailId = detail.orderBOMDetailId || '';
        const materialType = detail.materialType;

        row.innerHTML = `
        <td><input class="form-check-input row-checkbox" type="checkbox" disabled></td>
        <td><select class="form-select form-select-sm material-type-select is-locked" disabled><option>${materialType === 'FA' ? 'Fabric' : 'Trim'}</option></select></td>
        <td><select class="form-select form-select-sm material-group-select is-locked" disabled></select></td>
        <td><select class="form-select form-select-sm material-code-select is-locked" disabled></select></td>
        <td><input type="text" class="form-control form-control-sm material-name-input" value="${detail.materialName || ''}" readonly></td>
        <td><select class="form-select form-select-sm color-select is-locked" disabled></select></td>
        <td><select class="form-select form-select-sm size-select is-locked" disabled></select></td>
        <td><input type="text" class="form-control form-control-sm uom-input" value="${detail.uom || ''}" readonly></td>
        <td><input type="text" class="form-control form-control-sm text-end qty-input" value="${numberFormatter.format(detail.purchaseQuantity || 0)}" inputmode="decimal"></td>
        <td><input type="text" class="form-control form-control-sm text-end price-input" value="${numberFormatter.format(detail.netPrice || 0)}" inputmode="decimal"></td>
        <td><input type="text" class="form-control form-control-sm text-end tax-rate-input" value="${detail.taxRate || 0}" inputmode="decimal"></td>
        <td><input type="text" class="form-control form-control-sm text-end received-qty-input" value="${numberFormatter.format(detail.receivedQuantity || 0)}" inputmode="decimal"></td>
        <td><input type="text" class="form-control form-control-sm text-end line-amount" readonly></td>
        <td class="text-center"><button type="button" class="btn btn-sm btn-outline-danger delete-row-btn" disabled><i class="bi bi-trash"></i></button></td>
    `;

        const groupSelect = row.querySelector('.material-group-select');
        const codeSelect = row.querySelector('.material-code-select');
        const colorSelect = row.querySelector('.color-select');
        const sizeSelect = row.querySelector('.size-select');

        groupSelect.innerHTML = materialGroupTemplate.innerHTML;
        groupSelect.value = detail.materialGroupId || '';

        const groupId = detail.materialGroupId;
        if (materialType && groupId) {
            const cacheKey = `${materialType}-${groupId}`;
            if (!materialCodeCache[cacheKey]) {
                materialCodeCache[cacheKey] = await fetchApi(`/api/materials?type=${materialType}&materialGroupId=${groupId}`);
            }
            codeSelect.innerHTML = '<option value="">-- Select --</option>';
            materialCodeCache[cacheKey].forEach(m => codeSelect.add(new Option(m.code, m.id)));
            codeSelect.value = detail.fabricId || detail.trimId || '';
        }

        const materialId = codeSelect.value;
        if (materialType && materialId) {
            const cacheKey = `${materialType}-${materialId}`;
            if (!colorCache[cacheKey]) {
                colorCache[cacheKey] = await fetchApi(`/api/material-colors?type=${materialType}&materialId=${materialId}`);
            }
            colorSelect.innerHTML = '<option value="">-- Select --</option>';
            colorCache[cacheKey].forEach(c => colorSelect.add(new Option(c.code, c.code)));
            colorSelect.value = detail.colorCode || '';
        }

        const currentMaterialId = codeSelect.value;
        const currentColorCode = colorSelect.value;
        if (materialType === 'TR' && currentMaterialId && currentColorCode) {
            const cacheKey = `${currentMaterialId}-${currentColorCode}`;
            if (!sizeCache[cacheKey]) {
                sizeCache[cacheKey] = await fetchApi(`/api/material-sizes?trimId=${currentMaterialId}&colorCode=${currentColorCode}`);
            }
            sizeSelect.innerHTML = '<option value="">-- Select --</option>';
            sizeCache[cacheKey].forEach(s => sizeSelect.add(new Option(s.size, s.size)));
            sizeSelect.value = detail.size || '';
        }
    }

    /** Renders the initial state of the details table from a data array. */
    async function renderDetailsTable(details = []) {
        tableBody.innerHTML = '';
        if (details && details.length > 0) {
            for (const detail of details) {
                await createAndAppendRow(detail);
            }
        }
        updateAllLineAmounts();
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
            if (el.id === 'status' || el.classList.contains('is-locked')) {
                return;
            }

            const isAlwaysEditable = el.id === 'arrivalDate' || el.classList.contains('received-qty-input');
            if (isAlwaysEditable) {
                el.disabled = false;
                return;
            }
            el.disabled = readOnly;
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

        if (poData.arrivalDate) {
            let dateValue = poData.arrivalDate;
            if (Array.isArray(dateValue)) {
                const [year, month, day] = dateValue;
                dateValue = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            }
            arrivalDateInput.value = dateValue.split('T')[0];
        }

        // Step 3: Render the initial details table using the data from the server.
        await renderDetailsTable(poData.details);

        // Step 4: Determine the PO's status and set the form's interactivity.
        const status = statusInput.value;

        addDetailBtn.style.display = 'none';
        deleteSelectedDetailsBtn.style.display = 'none';

        if (status === 'New' || status === 'Rejected') {
            setFormReadOnly(false);
            saveBtn.style.display = 'inline-block';
            submitBtn.style.display = 'inline-block';
        } else if (status === 'Approved') {
            setFormReadOnly(true);
            saveBtn.style.display = 'inline-block';
            submitBtn.style.display = 'none';
        } else {
            setFormReadOnly(true);
            saveBtn.style.display = 'none';
            submitBtn.style.display = 'none';
        }

        // Step 5: Show the print button only for Approved POs.
        if (!isNew && status === 'Approved') {
            printBtn.style.display = 'inline-block';
        } else {
            printBtn.style.display = 'none';
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
            confirmButtonText: 'Delete'
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
            arrivalDate: arrivalDateInput.value || null,
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
            confirmButtonText: 'Submit'
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

