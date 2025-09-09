document.addEventListener('DOMContentLoaded', function() {
    // --- KHAI BÁO BIẾN VÀ ELEMENT ---
    const poData = JSON.parse(document.getElementById('po-data').textContent);
    console.log("Loaded PO Data from Server:", poData);

    // Form elements
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

    // --- CÁC HÀM GỌI API ---
    async function fetchApi(url, options = {}) {
        const defaultOptions = { headers: { 'Content-Type': 'application/json' } };
        const response = await fetch(url, { ...defaultOptions, ...options });
        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'API Error');
        }
        if (response.status === 204 || (response.status === 200 && options.method === 'DELETE')) return null;
        return response.json();
    }

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

    // --- CÁC HÀM XỬ LÝ GIAO DIỆN ---
    const numberFormatter = new Intl.NumberFormat('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

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

    function renderDetailsTable(details = []) {
        tableBody.innerHTML = '';
        if (details && details.length > 0) {
            details.forEach(detail => createAndAppendRow(detail));
        }
        updateAllLineAmounts();
    }

    // THAY THẾ hàm populateDropdownIfNeeded cũ bằng hàm này

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

                // Gán price và taxRate vào dataset của option
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

    // --- CÁC HÀM TÍNH TOÁN VÀ ĐỊNH DẠNG ---

    function updateLineAmount(row) {
        const qty = parseFloat(row.querySelector('.qty-input').value.replace(/,/g, '')) || 0;
        const price = parseFloat(row.querySelector('.price-input').value.replace(/,/g, '')) || 0;
        const taxRate = parseFloat(row.querySelector('.tax-rate-input').value.replace(/,/g, '')) || 0;
        const amount = qty * price * (1 + taxRate / 100.0);
        row.querySelector('.line-amount').value = numberFormatter.format(amount);
    }

    function updateAllLineAmounts() {
        tableBody.querySelectorAll('tr').forEach(updateLineAmount);
        updateTotalAmount();
    }

    function updateTotalAmount() {
        let total = 0;
        tableBody.querySelectorAll('.line-amount').forEach(input => {
            total += parseFloat(input.value.replace(/,/g, '')) || 0;
        });
        totalAmountInput.value = numberFormatter.format(total);
    }

    // Thêm hàm này vào khu vực "CÁC HÀM XỬ LÝ GIAO DIỆN"

    function updatePriceAndTaxFromSelection(selectElement) {
        const row = selectElement.closest('tr');
        const priceInput = row.querySelector('.price-input');
        const taxInput = row.querySelector('.tax-rate-input');

        const selectedOption = selectElement.options[selectElement.selectedIndex];

        // Nếu không chọn gì, reset giá trị
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

        // Gọi hàm tính lại tổng tiền của dòng và toàn bộ đơn hàng
        updateAllLineAmounts();
    }

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

    // --- HÀM KHỞI TẠO CHÍNH ---
    async function initializeForm() {
        await loadSuppliers();
        const isNew = !poData.purchaseOrderId;

        // Điền dữ liệu header
        poNumberSpan.textContent = poData.purchaseOrderNo || '(New)';
        idInput.value = poData.purchaseOrderId || '';
        poNoInput.value = poData.purchaseOrderNo || '';
        currencyInput.value = poData.currencyCode || '';
        deliveryTermInput.value = poData.deliveryTerm || '';
        paymentTermInput.value = poData.paymentTerm || '';
        statusInput.value = poData.status || 'New';

        if (poData.poDate) {
            // Code này xử lý cả trường hợp backend trả về dạng chuỗi "2025-08-31"
            // hay dạng mảng [2025, 8, 31]
            let dateValue = poData.poDate;
            if (Array.isArray(dateValue)) {
                const [year, month, day] = dateValue;
                dateValue = `${year}-${String(month).padStart(2, '0')}-${String(day).padStart(2, '0')}`;
            }
            // Đảm bảo chỉ lấy phần YYYY-MM-DD nếu có thông tin giờ giấc đi kèm
            poDateInput.value = dateValue.split('T')[0];
        } else if (isNew) {
            poDateInput.value = new Date().toISOString().split('T')[0];
        }

        renderDetailsTable(poData.details);

        const status = statusInput.value;
        if (status === 'New' || status === 'Rejected') {
            setFormReadOnly(false);
            saveBtn.style.display = 'inline-block';
            submitBtn.style.display = 'inline-block';
        } else {
            setFormReadOnly(true);
            saveBtn.style.display = 'none';
            submitBtn.style.display = 'none';
        }
        if (!isNew) {
            printBtn.style.display = 'inline-block';
        }

// Thêm sự kiện click cho nút Print
        printBtn.addEventListener('click', () => {
            const poId = idInput.value;
            if (poId) {
                window.open(`/purchase_orders/print-preview/${poId}`, '_blank');
            }
        });
    }

    // --- XỬ LÝ SỰ KIỆN ---

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
            // Với Vải (FA), giá được xác định ngay khi chọn màu
            const type = row.querySelector('.material-type-select').value;
            if (type === 'FA') {
                updatePriceAndTaxFromSelection(target);
            }
        } else if (target.classList.contains('size-select')) {
            // Với Phụ liệu (TR), giá được xác định khi chọn size
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

    saveBtn.addEventListener('click', async () => {
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

        try {
            const savedPo = await fetchApi('/api/purchase_orders', {
                method: 'POST',
                body: JSON.stringify(payload)
            });
            Swal.fire('Success!', 'Purchase Order saved successfully.', 'success').then(() => {
                window.location.href = `/purchase_orders/form?id=${savedPo.purchaseOrderId}`;
            });
        } catch (e) {
            Swal.fire('Error!', `Failed to save Purchase Order: ${e.message}`, 'error');
        }
    });

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
                    // SỬA DÒNG NÀY: Thêm /api vào trước URL
                    await fetchApi(`/api/purchase_orders/${poId}/submit`, { method: 'POST' });
                    Swal.fire('Submitted!', 'The PO has been submitted for approval.', 'success')
                        .then(() => window.location.reload());
                } catch (e) {
                    Swal.fire('Error!', `Could not submit the PO. ${e.message}`, 'error');
                }
            }
        });
    });

    // --- CHẠY KHỞI TẠO ---
    initializeForm();
});

