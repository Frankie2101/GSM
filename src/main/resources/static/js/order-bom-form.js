document.addEventListener('DOMContentLoaded', function() {
    // --- KHAI BÁO CÁC ELEMENT (Giữ nguyên) ---
    const bomTemplateSelect = document.getElementById('bomTemplateId');
    const saleOrderIdInput = document.getElementById('saleOrderId');
    const tableBody = document.getElementById('bomDetailTableBody');
    const addDetailBtn = document.getElementById('addDetailBtn');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const unitTemplate = document.getElementById('units-data-template');
    const supplierTemplate = document.getElementById('suppliers-data-template');
    const materialGroupTemplate = document.getElementById('material-groups-data-template');
    const materialListCache = { FA: null, TR: null };


    // --- CÁC HÀM GỌI API ---
    const fetchApi = async (url) => {
        try {
            const response = await fetch(url);
            if (!response.ok) throw new Error(`API call failed: ${url}`);
            return await response.json();
        } catch (error) { console.error(error); return []; }
    };
    const fetchMaterialList = (type) => fetchApi(`/api/materials?type=${type}`);
    const fetchMaterialDetails = (id, type) => fetchApi(`/api/material-details/${id}?type=${type}`);
    const fetchMaterialColors = (type, materialId) => fetchApi(`/api/material-colors?type=${type}&materialId=${materialId}`);
    const fetchMaterialSizes = (trimId, colorCode) => fetchApi(`/api/material-sizes?trimId=${trimId}&colorCode=${colorCode}`);

    // --- CÁC HÀM XỬ LÝ GIAO DIỆN ---

    // Hàm vẽ lại toàn bộ bảng từ dữ liệu
    async function renderDetailsTable(details) {
        tableBody.innerHTML = '';
        if (!details || details.length === 0) return;
        for (const detail of details) {
            await createAndInitRow(detail);
        }
        reindexRows();
    }

    // Hàm tạo và khởi tạo logic cho một dòng mới (Cập nhật HTML)
    // Thay thế hàm createAndInitRow bằng phiên bản mới này
    async function createAndInitRow(detail = {}) {
        const index = tableBody.rows.length;
        const row = tableBody.insertRow();
        const unitOptionsHtml = unitTemplate?.innerHTML || '<option value=""></option>';
        const supplierOptionsHtml = supplierTemplate?.innerHTML || '<option value=""></option>';
        const materialGroupOptionsHtml = materialGroupTemplate?.innerHTML || '<option value=""></option>';

        // Thay thế toàn bộ khối row.innerHTML trong hàm createAndInitRow
        row.innerHTML = `
    <td class="text-center align-middle"><input class="form-check-input row-checkbox" type="checkbox"></td>
    <td class="align-middle text-center seq-number"></td>
    
    <input type="hidden" name="details[${index}].orderBOMDetailId" value="${detail.orderBOMDetailId || ''}">
    <input type="hidden" name="details[${index}].seq">
    <input type="hidden" name="details[${index}].fabricId" value="${detail.fabricId || ''}">
    <input type="hidden" name="details[${index}].trimId" value="${detail.trimId || ''}">

    <td><select class="form-select form-select-sm material-group-select" name="details[${index}].materialGroupId">${materialGroupOptionsHtml}</select></td>
    <td>
        <select class="form-select form-select-sm material-type-select" name="details[${index}].materialType">
            <option value=""></option>
            <option value="FA" ${detail.materialType === 'FA' ? 'selected' : ''}>Fabric</option>
            <option value="TR" ${detail.materialType === 'TR' ? 'selected' : ''}>Trim</option>
        </select>
    </td>
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
        // Phần còn lại của hàm giữ nguyên...
        // Populate và chọn các dropdown tĩnh
        if (detail.materialGroupId) row.querySelector('.material-group-select').value = detail.materialGroupId;
        if (detail.uom) row.querySelector('.uom-select').value = detail.uom;

        const supplierSelect = row.querySelector('.supplier-select');
        if (detail.supplier) {
            supplierSelect.value = detail.supplier;
            const selectedSupplierOption = supplierSelect.options[supplierSelect.selectedIndex];
            if (selectedSupplierOption) {
                row.querySelector('.currency-input').value = selectedSupplierOption.dataset.currency || '';
            }
        }

        // Populate các dropdown động
        const codeSelect = row.querySelector('.material-code-select');
        if (detail.materialType) {
            const materials = await fetchMaterialList(detail.materialType);
            codeSelect.innerHTML = '<option value=""></option>';
            materials.forEach(m => {
                const rmId = detail.fabricId || detail.trimId;
                const option = new Option(m.code, m.id);
                if (rmId && m.id == rmId) { option.selected = true; }
                codeSelect.add(option);
            });

            if (codeSelect.value) {
                const colors = await fetchMaterialColors(detail.materialType, codeSelect.value);
                const colorSelect = row.querySelector('.color-code-select');
                colorSelect.innerHTML = '<option value=""></option>';
                colors.forEach(c => {
                    const option = new Option(c.code, c.code);
                    option.dataset.name = c.name;
                    option.dataset.price = c.price;
                    if(detail.colorCode && c.code === detail.colorCode) {
                        option.selected = true;
                    }
                    colorSelect.add(option);
                });
            }
        }
    }

    // Hàm đánh lại số thứ tự và tên các input
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

    // --- CÁC SỰ KIỆN ---

    // Sự kiện khi chọn BOM Template
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

    // Sự kiện khi nhấn nút "Add Detail"
    addDetailBtn.addEventListener('click', async function() {
        const soQty = tableBody.querySelector('.so-qty-input')?.value || 0;
        await createAndInitRow({ soQty: soQty });
        reindexRows();
    });

    // Sự kiện 'change' trên toàn bộ bảng để xử lý các dropdown
    tableBody.addEventListener('change', async function(e) {
        const target = e.target;
        const row = target.closest('tr');
        if (!row) return;

        const type = row.querySelector('.material-type-select').value;
        const materialId = row.querySelector('.material-code-select').value;
        const colorSelect = row.querySelector('.color-code-select');
        const sizeSelect = row.querySelector('.size-select');
        const priceInput = row.querySelector('.price-input');

        // Khi chọn Material Type
        if (target.classList.contains('material-type-select')) {
            const codeSelect = row.querySelector('.material-code-select');
            const materials = await fetchMaterialList(type);
            codeSelect.innerHTML = '<option value=""></option>';
            materials.forEach(m => codeSelect.add(new Option(m.code, m.id)));
            // Reset tất cả các trường phụ thuộc
            row.querySelector('.material-name-input').value = '';
            row.querySelector('.uom-select').value = '';
            row.querySelector('.supplier-select').value = '';
            colorSelect.innerHTML = '';
            sizeSelect.innerHTML = '';
            priceInput.value = '';
        }

        // Khi chọn Material Code
        if (target.classList.contains('material-code-select')) {
            if (materialId) {
                const details = await fetchMaterialDetails(materialId, type);
                if (details) {
                    row.querySelector('.material-name-input').value = details.name || '';
                    row.querySelector('.uom-select').value = details.unitName || '';

                    // === FIX: THÊM LOGIC CẬP NHẬT CURRENCY TẠI ĐÂY ===
                    const supplierSelect = row.querySelector('.supplier-select');
                    supplierSelect.value = details.supplier || ''; // Gán giá trị supplier

                    // Lấy option đang được chọn để đọc data-currency
                    const selectedSupplierOption = supplierSelect.options[supplierSelect.selectedIndex];
                    if (selectedSupplierOption) {
                        row.querySelector('.currency-input').value = selectedSupplierOption.dataset.currency || '';
                    } else {
                        row.querySelector('.currency-input').value = ''; // Nếu không tìm thấy supplier thì xóa currency
                    }
                    // === KẾT THÚC FIX ===                    // Price sẽ được cập nhật sau khi chọn Color/Size
                }

                // Load danh sách Color
                const colors = await fetchMaterialColors(type, materialId);
                colorSelect.innerHTML = '<option value=""></option>';
                colors.forEach(c => {
                    const option = new Option(c.code, c.code);
                    option.dataset.name = c.name;
                    option.dataset.price = c.price;
                    colorSelect.add(option);
                });

                // Xử lý Size
                sizeSelect.innerHTML = '<option value=""></option>';
                if(type === 'FA') {
                    sizeSelect.disabled = true;
                    sizeSelect.value = ''; // Reset giá trị
                } else {
                    sizeSelect.disabled = false;
                }
            }
        }

        // Khi chọn Color
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

        // Khi chọn Size (chỉ cho Trim)
        if (target.classList.contains('size-select')) {
            const selectedSize = target.options[target.selectedIndex];
            priceInput.value = selectedSize.dataset.price || '';
        }

        // Khi chọn Supplier
        if (target.classList.contains('supplier-select')) {
            const selectedSupplier = target.options[target.selectedIndex];
            row.querySelector('.currency-input').value = selectedSupplier.dataset.currency || '';
        }
    });

    // Sự kiện 'input' để tính toán lại các loại số lượng
    tableBody.addEventListener('input', function(e) {
        if (['usage-input', 'waste-input', 'so-qty-input', 'inventory-qty-input'].some(c => e.target.classList.contains(c))) {
            const row = e.target.closest('tr');
            if (!row) return;

            const usage = parseFloat(row.querySelector('.usage-input').value) || 0;
            const waste = parseFloat(row.querySelector('.waste-input').value) || 0;
            const soQty = parseFloat(row.querySelector('.so-qty-input').value) || 0;
            const inventoryQty = parseFloat(row.querySelector('.inventory-qty-input').value) || 0;

            const demandQty = soQty * usage * (1 + waste / 100.0);
            row.querySelector('.demand-qty-input').value = demandQty.toFixed(2);

            const purchaseQty = demandQty - inventoryQty;
            row.querySelector('.purchase-qty-input').value = (purchaseQty > 0 ? purchaseQty : 0).toFixed(2);
        }
    });

    // Các sự kiện xóa và select all
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

    // --- LOGIC KHỞI TẠO BAN ĐẦU ---
    // Đọc dữ liệu JSON đã có sẵn và vẽ lại bảng
    const initialDataEl = document.getElementById('initial-bom-details');
    if (initialDataEl) {
        try {
            const initialDetails = JSON.parse(initialDataEl.textContent);
            if(initialDetails && initialDetails.length > 0) {
                renderDetailsTable(initialDetails);
            }
        } catch (e) {
            console.error("Could not parse initial BOM details JSON.", e);
        }
    }

    // ===============================================
// LOGIC CHO CHỨC NĂNG GENERATE PURCHASE ORDER
// ===============================================

// Lấy các element cần thiết cho modal
    const generatePoBtn = document.getElementById('generatePoBtn');
    const poPreviewTableBody = document.getElementById('poPreviewTableBody');
// Khởi tạo đối tượng modal của Bootstrap
    const generatePoModal = new bootstrap.Modal(document.getElementById('generatePoModal'));

// Bắt sự kiện click vào nút "Generate PO"
    // Bắt sự kiện click vào nút "Generate PO"
    if (generatePoBtn) {
        generatePoBtn.addEventListener('click', function() {

            // 1. Thu thập dữ liệu từ tất cả các dòng trong bảng BOM Details
            const allRows = tableBody.querySelectorAll('tr');
            const bomDetails = [];
            allRows.forEach(row => {
                // FIX 1: Chỉ lấy Material Group khi đã được chọn (value khác rỗng)
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

            // 2. Lọc ra các dòng hợp lệ để tạo PO
            const validDetailsForPO = bomDetails.filter(d => {
                const hasBaseInfo = d.purchaseQty > 0 && d.supplier && d.price > 0 && d.currency && d.uom && d.colorCode;
                if (!hasBaseInfo) return false;
                if (d.materialType === 'TR' && !d.size) return false;
                return true;
            });

            // FIX 4: Sắp xếp danh sách hợp lệ theo tên Supplier
            validDetailsForPO.sort((a, b) => {
                return a.supplier.localeCompare(b.supplier);
            });

            // 3. Hiển thị kết quả lên Modal
            poPreviewTableBody.innerHTML = '';

            if (validDetailsForPO.length === 0) {
                Swal.fire({
                    icon: 'warning',
                    title: 'No Valid Items Found',
                    text: 'Không tìm thấy dòng nào hợp lệ để tạo PO. Vui lòng kiểm tra lại các điều kiện (Purchase Qty > 0, đã chọn đủ màu sắc, size, nhà cung cấp, giá, đơn vị...).'
                });
                return;
            }

            validDetailsForPO.forEach(d => {
                const rowHtml = `
                <tr>
                    <td>${d.materialGroup}</td>
                    <td>${d.materialType}</td>
                    <td>${d.materialCode}</td>
                    <td>${d.materialName}</td>
                    <td>${d.colorCode}</td>
                    <td>${d.colorName}</td>
                    <td>${d.size || 'N/A'}</td>
                    <td>${d.uom}</td>
                    <td>${d.supplier}</td>
                    <td>${d.price}</td>
                    <td>${d.currency}</td>
                    <td><strong>${d.purchaseQty.toFixed(2)}</strong></td>
                </tr>
            `;
                poPreviewTableBody.insertAdjacentHTML('beforeend', rowHtml);
            });

            // 4. Mở Modal
            generatePoModal.show();
        });
    }

    // === LOGIC CHO NÚT CONFIRM GENERATE PO ===

// Lấy nút Confirm và form chính
    const confirmPoGenerationBtn = document.getElementById('confirmPoGenerationBtn');
    const orderBOMForm = document.getElementById('orderBOMForm');

    if (confirmPoGenerationBtn) {
        confirmPoGenerationBtn.addEventListener('click', async function() {

            // Hiển thị thông báo đang xử lý
            Swal.fire({
                title: 'Processing...',
                text: 'System is saving BOM and generating Purchase Orders.',
                allowOutsideClick: false,
                didOpen: () => {
                    Swal.showLoading();
                }
            });

            // Tạo một đối tượng FormData từ form chính
            const formData = new FormData(orderBOMForm);

            try {
                // Gửi yêu cầu POST đến endpoint mới
                const response = await fetch('/order-boms/generate-pos', {
                    method: 'POST',
                    body: new URLSearchParams(formData) // Gửi dữ liệu form
                });

                const result = await response.json();

                if (response.ok) {
                    // Nếu thành công, hiển thị thông báo và chuyển hướng
                    Swal.fire({
                        icon: 'success',
                        title: 'Success!',
                        text: result.message,
                    }).then(() => {
                        // Chuyển về trang Sale Order sau khi thành công
                        window.location.href = `/sale-orders/form?id=${saleOrderIdInput.value}`;
                    });
                } else {
                    // Nếu có lỗi từ server, hiển thị lỗi
                    throw new Error(result.message || 'An unknown error occurred.');
                }

            } catch (error) {
                Swal.fire({
                    icon: 'error',
                    title: 'Operation Failed',
                    text: error.message,
                });
            }
        });
    }

});