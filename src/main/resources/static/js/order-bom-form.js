/**
 * @fileoverview Manages the complex, dynamic UI for the Order BOM form.
 * Handles cascading dropdowns, dynamic row creation, real-time calculations,
 * and the PO generation preview modal.
 */
document.addEventListener('DOMContentLoaded', function() {

    // --- 1. INITIALIZATION: Get all required UI elements and prepare cache ---
    const bomTemplateSelect = document.getElementById('bomTemplateId');
    const saleOrderIdInput = document.getElementById('saleOrderId');
    const tableBody = document.getElementById('bomDetailTableBody');
    const addDetailBtn = document.getElementById('addDetailBtn');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const unitTemplate = document.getElementById('units-data-template');
    const supplierTemplate = document.getElementById('suppliers-data-template');
    const materialGroupTemplate = document.getElementById('material-groups-data-template');

    //Read data from meta tag
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    const generatePoBtn = document.getElementById('generatePoBtn');
    const poPreviewTableBody = document.getElementById('poPreviewTableBody');
    const generatePoModal = new bootstrap.Modal(document.getElementById('generatePoModal'));
    const confirmPoGenerationBtn = document.getElementById('confirmPoGenerationBtn');
    const orderBOMForm = document.getElementById('orderBOMForm');
    const materialListCache = { FA: null, TR: null };

    // --- ELEMENT SELECTORS & CACHE INITIALIZATION ---

    /**
     * --- 2. API HELPERS: Centralized functions for calling backend APIs ---
     * A generic fetch wrapper to handle CSRF tokens and JSON parsing automatically.
     * @param {string} url - The API endpoint to call.
     * @param {object} options - Standard fetch options (method, body, etc.).
     * @returns {Promise<object|string|null>} The parsed JSON response or null on error.
     */
    const fetchApi = async (url, options = {}) => {
        try {
            //Add Meta into Header
            const headers = { ...options.headers, [csrfHeader]: csrfToken };
            const response = await fetch(url, { ...options, headers });

            if (!response.ok) throw new Error(`API call failed: ${response.statusText}`);
            const contentType = response.headers.get("content-type");
            return contentType?.includes("application/json") ? await response.json() : await response.text();
        } catch (error) {
            console.error(error);
            return null;
        }
    };

    const fetchMaterialList = (type) => fetchApi(`/api/materials?type=${type}`);
    const fetchMaterialDetails = (id, type) => fetchApi(`/api/material-details/${id}?type=${type}`);
    const fetchMaterialColors = (type, materialId) => fetchApi(`/api/material-colors?type=${type}&materialId=${materialId}`);
    const fetchMaterialSizes = (trimId, colorCode) => fetchApi(`/api/material-sizes?trimId=${trimId}&colorCode=${colorCode}`);

    // --- 3. UI RENDERING & LOGIC FUNCTIONS ---

    /**
     * Renders the entire details table from a list of detail objects.
     * @param {Array<object>} details - The list of BOM detail data.
     */
    async function renderDetailsTable(details) {
        tableBody.innerHTML = '';
        if (!details || details.length === 0) return;
        for (const detail of details) {
            await createAndInitRow(detail);
        }
        reindexRows();
    }

    /**
     * Creates, initializes, and appends a single new row to the details table.
     * @param {object} detail - Optional data object for an existing detail line.
     */
    async function createAndInitRow(detail = {}) {
        const index = tableBody.rows.length;
        const row = tableBody.insertRow();
        const unitOptionsHtml = unitTemplate?.innerHTML || '<option value=""></option>';
        const supplierOptionsHtml = supplierTemplate?.innerHTML || '<option value=""></option>';
        const materialGroupOptionsHtml = materialGroupTemplate?.innerHTML || '<option value=""></option>';

        row.innerHTML = `
            <td class="text-center align-middle"><input class="form-check-input row-checkbox" type="checkbox"></td>
            <td class="align-middle text-center seq-number"></td>
            <input type="hidden" name="details[${index}].orderBOMDetailId" value="${detail.orderBOMDetailId || ''}">
            <input type="hidden" name="details[${index}].seq">
            <input type="hidden" name="details[${index}].fabricId" value="${detail.fabricId || ''}">
            <input type="hidden" name="details[${index}].trimId" value="${detail.trimId || ''}">
            <td><select class="form-select form-select-sm material-group-select" name="details[${index}].materialGroupId">${materialGroupOptionsHtml}</select></td>
            <td><select class="form-select form-select-sm material-type-select" name="details[${index}].materialType"><option value=""></option><option value="FA" ${detail.materialType === 'FA' ? 'selected' : ''}>Fabric</option><option value="TR" ${detail.materialType === 'TR' ? 'selected' : ''}>Trim</option></select></td>
            <td><select class="form-select form-select-sm material-code-select" name="details[${index}].materialCode"></select></td>
            <td><input type="text" class="form-control form-control-sm material-name-input" name="details[${index}].materialName" value="${detail.materialName || ''}" readonly></td>
            <td><select class="form-select form-select-sm color-code-select" name="details[${index}].colorCode"></select></td>
            <td><input type="text" class="form-control form-control-sm color-name-input" name="details[${index}].colorName" value="${detail.colorName || ''}" readonly></td>
            <td><select class="form-select form-select-sm size-select" name="details[${index}].size"></select></td>
            <td><select class="form-select form-select-sm uom-select" name="details[${index}].uom">${unitOptionsHtml}</select></td>
            <td><select class="form-select form-select-sm supplier-select" name="details[${index}].supplier">${supplierOptionsHtml}</select></td>
            <td><input type="number" class="form-control form-control-sm price-input" name="details[${index}].price" value="${detail.price || ''}" step="0.01"></td>
            <td><input type="text" class="form-control form-control-sm currency-input" name="details[${index}].currency" value="${detail.currency || ''}" maxlength="3" readonly></td>
            <td><input type="number" class="form-control form-control-sm usage-input" name="details[${index}].usageValue" value="${detail.usageValue || 0}" step="0.001"></td>
            <td><input type="number" class="form-control form-control-sm waste-input" name="details[${index}].waste" value="${detail.waste || 0}" step="0.01"></td>
            <td><input type="number" class="form-control form-control-sm so-qty-input" name="details[${index}].soQty" value="${detail.soQty || 0}"></td>
            <td><input type="number" class="form-control form-control-sm demand-qty-input" name="details[${index}].demandQty" value="${detail.demandQty || 0}" step="0.0001" readonly></td>
            <td><input type="number" step="0.01" class="form-control form-control-sm inventory-qty-input" name="details[${index}].inventoryQty" value="${(detail.inventoryQty || 0).toFixed(2)}"></td>
            <td><input type="number" step="0.01" class="form-control form-control-sm purchase-qty-input" name="details[${index}].purchaseQty" value="${(detail.purchaseQty || 0).toFixed(2)}"></td>
            <td class="text-center align-middle"><button type="button" class="btn btn-sm delete-row-btn"><i class="bi bi-trash"></i></button></td>
        `;

        //Set value to dropdown
        if (detail.materialGroupId) row.querySelector('.material-group-select').value = detail.materialGroupId;
        if (detail.uom) row.querySelector('.uom-select').value = detail.uom;

        const supplierSelect = row.querySelector('.supplier-select');
        if (detail.supplier) {
            supplierSelect.value = detail.supplier;
            const selectedSupplierOption = supplierSelect.options[supplierSelect.selectedIndex];
            //Set currency based on selected supplier
            if (selectedSupplierOption) {
                row.querySelector('.currency-input').value = selectedSupplierOption.dataset.currency || '';
            }
        }

        const codeSelect = row.querySelector('.material-code-select');
        if (detail.materialType) {
            //Get the material list based on material type
            const materials = await fetchMaterialList(detail.materialType);
            codeSelect.innerHTML = '<option value=""></option>'; //Clear previous selected option
            materials.forEach(m => {
                const rmId = detail.fabricId || detail.trimId;
                const option = new Option(m.code, m.id);
                if (rmId && m.id == rmId) { option.selected = true; }
                codeSelect.add(option);
            });

            //Once selected material code
            if (codeSelect.value) {
                //Get the color list based on material code
                const colors = await fetchMaterialColors(detail.materialType, codeSelect.value);
                const colorSelect = row.querySelector('.color-code-select');
                colorSelect.innerHTML = '<option value=""></option>';
                colors.forEach(c => {
                    const option = new Option(c.name ? `${c.code} - ${c.name}` : c.code, c.code);
                    option.dataset.name = c.name;
                    option.dataset.price = c.price;
                    if(detail.colorCode && c.code === detail.colorCode) { option.selected = true; }
                    colorSelect.add(option);
                });
                colorSelect.dispatchEvent(new Event('change'));
            }
        }
    }

    /**
     * Re-indexes all rows to ensure form input names are sequential (e.g., details[0], details[1]).
     */
    function reindexRows() {
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            row.querySelector('.seq-number').textContent = index + 1;
            row.querySelectorAll('[name^="details["]').forEach(input => {
                const name = input.getAttribute('name');
                const newName = name.replace(/details\[\d+\]/, `details[${index}]`);
                input.setAttribute('name', newName);
            });
            row.querySelector('input[name*="seq"]').value = index + 1;
        });
    }

    /**
     * Handles when the user selects a BOM Template to auto-populate the details table.
     * Fetches the preview details for the selected template and renders the table.
     */
    bomTemplateSelect.addEventListener('change', async function() {
        const bomTemplateId = this.value;
        const saleOrderId = saleOrderIdInput.value;
        if (!bomTemplateId) { tableBody.innerHTML = ''; return; }
        try {
            const response = await fetch(`/api/order-boms/generate-preview?saleOrderId=${saleOrderId}&bomTemplateId=${bomTemplateId}`);
            if (!response.ok) throw new Error('API Error');
            const data = await response.json();
            await renderDetailsTable(data.details);
        } catch (error) { console.error('Error:', error); }
    });

    /**
     * Event listener for the "Add Detail" button. Creates a new blank row.
     */
    addDetailBtn.addEventListener('click', async function() {
        const soQty = tableBody.querySelector('.so-qty-input')?.value || 0;
        await createAndInitRow({ soQty: soQty });
        reindexRows();
    });

    /**
     * Main event listener for cascading logic, using event delegation on the table body.
     * This single listener handles changes for all dropdowns in all rows.
     */
    tableBody.addEventListener('change', async function(e) {
        const target = e.target;
        const row = target.closest('tr');
        if (!row) return;

        const type = row.querySelector('.material-type-select').value;
        const materialId = row.querySelector('.material-code-select').value;
        const colorSelect = row.querySelector('.color-code-select');
        const sizeSelect = row.querySelector('.size-select');
        const priceInput = row.querySelector('.price-input');

        //Once change Material Type, call API to get the Material Code list and clear the previous reference
        //value such as Material Name, UOM, ...
        if (target.classList.contains('material-type-select')) {
            const codeSelect = row.querySelector('.material-code-select');
            const materials = await fetchMaterialList(type);
            codeSelect.innerHTML = '<option value=""></option>';
            materials.forEach(m => codeSelect.add(new Option(m.code, m.id)));
            row.querySelector('.material-name-input').value = '';
            row.querySelector('.uom-select').value = '';
            row.querySelector('.supplier-select').value = '';
            colorSelect.innerHTML = '';
            sizeSelect.innerHTML = '';
            priceInput.value = '';
        }

        //Once change Material Code, automatically populate Material Name, Supplier, UOM
        if (target.classList.contains('material-code-select')) {
            if (materialId) {
                const details = await fetchMaterialDetails(materialId, type);
                if (details) {
                    row.querySelector('.material-name-input').value = details.name || '';
                    row.querySelector('.uom-select').value = details.unitName || '';
                    const supplierSelect = row.querySelector('.supplier-select');
                    supplierSelect.value = details.supplier || '';
                    supplierSelect.dispatchEvent(new Event('change'));
                }

                //Get the color for dropdown list
                const colors = await fetchMaterialColors(type, materialId);
                colorSelect.innerHTML = '<option value=""></option>';
                colors.forEach(c => {
                    const option = new Option(c.name ? `${c.code} - ${c.name}` : c.code, c.code);
                    option.dataset.name = c.name;
                    option.dataset.price = c.price;
                    colorSelect.add(option);
                });
                sizeSelect.innerHTML = '<option value=""></option>';
                //Disable Size for Fabric
                if(type === 'FA') {
                    sizeSelect.disabled = true;
                    sizeSelect.value = '';
                } else {
                    sizeSelect.disabled = false;
                }
            }
        }

        //Once select Color and Size, automatically populate Price
        if (target.classList.contains('color-code-select')) {
            const selectedColor = target.options[target.selectedIndex];
            row.querySelector('.color-name-input').value = selectedColor.dataset.name || '';
            if (type === 'FA') {
                priceInput.value = selectedColor.dataset.price || '';
            } else if (type === 'TR') {
                const sizes = await fetchMaterialSizes(materialId, target.value);
                sizeSelect.innerHTML = '<option value=""></option>';
                sizes.forEach(s => {
                    const option = new Option(s.size, s.size);
                    option.dataset.price = s.price;
                    sizeSelect.add(option);
                });
            }
        }

        if (target.classList.contains('size-select')) {
            const selectedSize = target.options[target.selectedIndex];
            priceInput.value = selectedSize.dataset.price || '';
        }

        if (target.classList.contains('supplier-select')) {
            const selectedSupplier = target.options[target.selectedIndex];
            row.querySelector('.currency-input').value = selectedSupplier.dataset.currency || '';
        }
    });

    /**
     * Listener for real-time calculations when quantity-related fields are modified.
     * Recalculates demand and purchase quantities whenever relevant inputs change.
     */
    tableBody.addEventListener('input', function(e) {
        //Once change Usage / Waste / SO Qty / Inventory Qty
        if (['usage-input', 'waste-input', 'so-qty-input', 'inventory-qty-input'].some(c => e.target.classList.contains(c))) {
            //Get those data in a row
            const row = e.target.closest('tr');
            if (!row) return;
            const usage = parseFloat(row.querySelector('.usage-input').value) || 0;
            const waste = parseFloat(row.querySelector('.waste-input').value) || 0;
            const soQty = parseFloat(row.querySelector('.so-qty-input').value) || 0;
            const inventoryQty = parseFloat(row.querySelector('.inventory-qty-input').value) || 0;

            //Calculate Demand Qty based on updated value
            const demandQty = soQty * usage * (1 + waste / 100.0);
            row.querySelector('.demand-qty-input').value = demandQty.toFixed(2);

            //Calculate Purchase Qty based on updated Demand Qty
            const purchaseQty = demandQty - inventoryQty;
            row.querySelector('.purchase-qty-input').value = (purchaseQty > 0 ? purchaseQty : 0).toFixed(2);
        }
    });

    deleteSelectedBtn.addEventListener('click', function() {
        const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
        if (checkedBoxes.length === 0) { Swal.fire('No selection', 'Please select rows to delete.', 'warning'); return; }
        checkedBoxes.forEach(box => box.closest('tr').remove());
        reindexRows();
    });

    tableBody.addEventListener('click', function(e) {
        if (e.target.closest('.delete-row-btn')) {
            e.target.closest('tr').remove();
            reindexRows();
        }
    });

    selectAllCheckbox.addEventListener('change', function() {
        tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => checkbox.checked = this.checked);
    });

    // --- INITIALIZATION LOGIC ---

    /**
     *  This block runs on page load to populate the form if editing an existing BOM.
     */
    const initialDataEl = document.getElementById('initial-bom-details');
    if (initialDataEl) {
        try {
            const initialDetails = JSON.parse(initialDataEl.textContent);
            if(initialDetails && initialDetails.length > 0) {
                renderDetailsTable(initialDetails);
            }
        } catch (e) { console.error("Could not parse initial BOM details JSON.", e); }
    }

    // --- 6. PO GENERATION MODAL LOGIC ---

    /**
     * Event listener for the "Generate PO" button.
     * It gathers data from the UI, filters for valid PO lines, sorts them by supplier,
     * and displays a preview in the modal.
     */
    if (generatePoBtn) {
        generatePoBtn.addEventListener('click', function() {
            // 1. Collect the data from all visible rows in the Order BOM Detail table.
            const allRows = tableBody.querySelectorAll('tr');
            const bomDetails = [];
            allRows.forEach(row => {
                const materialGroupSelect = row.querySelector('.material-group-select');
                const materialGroupText = materialGroupSelect.value ? materialGroupSelect.options[materialGroupSelect.selectedIndex].textContent.trim() : '';
                const detail = {
                    materialGroup: materialGroupText,
                    materialType: row.querySelector('.material-type-select').value,
                    materialCode: row.querySelector('.material-code-select option:checked').textContent.trim(),
                    materialName: row.querySelector('.material-name-input').value,
                    colorCode: row.querySelector('.color-code-select').value,
                    colorName: row.querySelector('.color-name-input').value,
                    size: row.querySelector('.size-select').value,
                    uom: row.querySelector('.uom-select').value,
                    supplier: row.querySelector('.supplier-select').value,
                    price: parseFloat(row.querySelector('.price-input').value) || 0,
                    currency: row.querySelector('.currency-input').value,
                    purchaseQty: parseFloat(row.querySelector('.purchase-qty-input').value) || 0,
                };
                bomDetails.push(detail);
            });

            // 2. Filter for rows that are valid for PO generation based on business rules.
            const validDetailsForPO = bomDetails.filter(d => {
                const hasBaseInfo = d.purchaseQty > 0 && d.supplier && d.price > 0 && d.currency && d.uom && d.colorCode;
                if (!hasBaseInfo) return false;
                if (d.materialType === 'TR' && !d.size) return false;
                return true;
            });

            // 3. Sort the valid rows by supplier to group them in the preview.
            validDetailsForPO.sort((a, b) => a.supplier.localeCompare(b.supplier));

            // 4. Clear the previous preview and check if there are any valid items.
            poPreviewTableBody.innerHTML = '';
            if (validDetailsForPO.length === 0) {
                // If no valid items, display a warning message.
                Swal.fire({ icon: 'warning', title: 'No Valid Items Found', text: 'Can not find any valid items. Please make sure that Purchase Qty is greater than 0 and all the required information have been fulfilled).' });
                return;
            }

            // 5. Build the preview table HTML from the valid, sorted data.
            validDetailsForPO.forEach(d => {
                const rowHtml = `
                <tr>
                    <td>${d.materialGroup}</td><td>${d.materialType}</td><td>${d.materialCode}</td>
                    <td>${d.materialName}</td><td>${d.colorCode}</td><td>${d.colorName}</td>
                    <td>${d.size || 'N/A'}</td><td>${d.uom}</td><td>${d.supplier}</td>
                    <td>${d.price}</td><td>${d.currency}</td><td><strong>${d.purchaseQty.toFixed(2)}</strong></td>
                </tr>
            `;
                poPreviewTableBody.insertAdjacentHTML('beforeend', rowHtml);
            });

            // 6. Show the preview modal.
            generatePoModal.show();
        });
    }

    /**
     * Handles the final confirmation click in the PO generation modal.
     */
    if (confirmPoGenerationBtn) {
        confirmPoGenerationBtn.addEventListener('click', async function() {
            // 1. Show a "Processing..." popup.
            Swal.fire({ title: 'Processing...', text: 'System is saving BOM and generating Purchase Orders.', allowOutsideClick: false, didOpen: () => { Swal.showLoading(); }});

            // 2. Collect all data from the main BOM form.
            const formData = new FormData(orderBOMForm);
            try {
                // 3. Submit the data to the PO generation API endpoint.
                const response = await fetch('/api/order-boms/generate-pos', {
                    method: 'POST',
                    headers: {
                        [csrfHeader]: csrfToken
                    },
                    body: new URLSearchParams(formData)
                });

                const result = await response.json();

                // 4. Handle the API response.
                if (response.ok) {
                    // On success, show a success message and redirect.
                    Swal.fire({ icon: 'success', title: 'Success!', text: result.message, }).then(() => {
                        window.location.href = `/sale-orders/form?id=${saleOrderIdInput.value}`;
                    });
                } else {
                    // On failure, throw an error to be caught below.
                    throw new Error(result.message || 'An unknown error occurred.');
                }
            } catch (error) {
                // 5. Display a generic error message if the API call fails.
                Swal.fire({ icon: 'error', title: 'Operation Failed', text: error.message, });
            }
        });
    }
});