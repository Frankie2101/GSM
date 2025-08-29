document.addEventListener('DOMContentLoaded', function() {
    const detailsContainer = document.getElementById('detailsContainer');
    const addDetailBtn = document.getElementById('addDetailBtn');
    const customerSelect = document.getElementById('customerId');
    const orderNoInput = document.getElementById('saleOrderNo');
    const currencyInput = document.getElementById('currencyCode');
    const orderDateInput = document.getElementById('orderDate');
    let detailIndex = 0;

    // YÊU CẦU 2: Tự động điền ngày hôm nay nếu là đơn hàng mới
    if (!document.querySelector('[name="saleOrderId"]').value) {
        const today = new Date().toISOString().split('T')[0];
        orderDateInput.value = today;
    }

    // YÊU CẦU 1 & 3: Tự động điền Order No và Currency khi chọn Customer
    customerSelect.addEventListener('change', async function() {
        const selectedOption = this.options[this.selectedIndex];
        const customerCode = selectedOption.dataset.code;
        const currency = selectedOption.dataset.currency;

        currencyInput.value = currency || '';

        if (customerCode && !document.querySelector('[name="saleOrderId"]').value) {
            try {
                const response = await fetch(`/api/sale-orders/next-order-no?customerCode=${customerCode}`);
                if (!response.ok) throw new Error('API response not OK');
                const nextOrderNo = await response.text();
                orderNoInput.value = nextOrderNo;
            } catch (error) {
                console.error('Failed to fetch next order number:', error);
                orderNoInput.value = customerCode + '00001'; // Fallback
            }
        }
    });

    // --- API Cache & Fetching Functions ---
    const apiCache = { products: null, colors: {}, sizes: {} };

    const fetchProducts = async () => {
        if (!apiCache.products) {
            const response = await fetch('/api/sale-orders/products');
            apiCache.products = await response.json();
        }
        return apiCache.products;
    };

    const fetchColors = async (productId) => {
        if (!apiCache.colors[productId]) {
            const response = await fetch(`/api/sale-orders/product-colors?productId=${productId}`);
            apiCache.colors[productId] = await response.json();
        }
        return apiCache.colors[productId];
    };

    const fetchSizes = async (productId, color) => {
        const key = `${productId}_${color}`;
        if (!apiCache.sizes[key]) {
            const response = await fetch(`/api/sale-orders/product-sizes?productId=${productId}&color=${color}`);
            apiCache.sizes[key] = await response.json();
        }
        return apiCache.sizes[key];
    };


// HÃY THAY THẾ HÀM createDetailCard BẰNG PHIÊN BẢN NÀY
    const createDetailCard = () => {
        const card = document.createElement('div');
        card.className = 'card mb-2 detail-card';
        card.dataset.index = detailIndex++;
        card.innerHTML = `
        <div class="card-body">
            <div class="d-flex align-items-begin">
                <div style="width: 13%;" class="me-3">
                    <label class="form-label form-label-sm mb-0">Product</label>
                    <select class="form-select form-select-sm product-select" title="Product"></select>
                </div>
                <div style="width: 17%;" class="me-3">
                    <label class="form-label form-label-sm mb-0">Product Name</label>
                    <input type="text" class="form-control form-control-sm product-name" readonly title="Product Name">
                </div>
                <div style="width: 10%;" class="me-3">
                    <label class="form-label form-label-sm mb-0">Color</label>
                    <select class="form-select form-select-sm color-select" title="Color"></select>
                </div>
                <div style="width: 10%;" class="me-3">
                    <label class="form-label form-label-sm mb-0">Color Name</label>
                    <input type="text" class="form-control form-control-sm color-name" readonly title="Color Name">
                </div>
                <div style="width: 7%;" class="me-3">
                     <label class="form-label form-label-sm mb-0">Unit</label>
                     <input type="text" class="form-control form-control-sm unit-name" readonly title="Unit">
                </div>
                <div class="flex-grow-1" style="min-width: 300px;">
                     <div class="size-matrix-container"></div>
                </div>
                <div class="ms-auto ps-2">
                    <button type="button" class="btn btn-sm btn-outline-danger delete-detail-btn"><i class="bi bi-trash"></i></button>
                </div>
            </div>
            <input type="hidden" name="details[${card.dataset.index}].productId">
            <input type="hidden" name="details[${card.dataset.index}].color">
        </div>
    `;
        detailsContainer.appendChild(card);
        return card;
    };

    // HÃY THAY THẾ TOÀN BỘ HÀM buildSizeMatrix CŨ BẰNG HÀM NÀY
    const buildSizeMatrix = async (card, productId, color, existingDetail = null) => {
        const sizes = await fetchSizes(productId, color);
        const container = card.querySelector('.size-matrix-container');
        const index = card.dataset.index;

        if (!sizes || sizes.length === 0) {
            container.innerHTML = '<small class="text-muted">No sizes for this color.</small>';
            return;
        }

        // Bắt đầu tạo HTML cho grid
        let gridHtml = '<div class="size-grid-container">';

        // --- Hàng 1: Headers (Ô trống + XS, S, M...) ---
        gridHtml += '<div class="grid-header"></div>'; // Ô trống góc trên bên trái
        sizes.forEach(s => {
            gridHtml += `<div class="grid-header">${s.size}</div>`;
        });

        // --- Hàng 2: Qty ---
        gridHtml += '<div class="grid-label">Order Qty</div>';
        sizes.forEach(s => {
            const qty = existingDetail && existingDetail.quantities && existingDetail.quantities[s.size] ? existingDetail.quantities[s.size] : '';
            gridHtml += `<div class="grid-input-cell"><input type="number" name="details[${index}].quantities[${s.size}]" value="${qty}" min="0"></div>`;
        });

        // --- Hàng 3: Price ---
        gridHtml += '<div class="grid-label">Price</div>';
        sizes.forEach(s => {
            const price = existingDetail && existingDetail.prices && existingDetail.prices[s.size] ? existingDetail.prices[s.size] : s.price;
            gridHtml += `<div class="grid-input-cell"><input type="number" step="0.01" name="details[${index}].prices[${s.size}]" value="${price || ''}"></div>`;
        });

        // --- Hàng 4: Ship Qty (Hàng cuối) ---
        gridHtml += '<div class="grid-label grid-row-end">Ship Qty</div>';
        sizes.forEach((s, i) => {
            const shipQty = existingDetail && existingDetail.shipQuantities && existingDetail.shipQuantities[s.size] ? existingDetail.shipQuantities[s.size] : '';
            // Thêm class grid-row-end cho các ô cuối cùng của hàng
            const endClass = (i === sizes.length - 1) ? 'grid-row-end' : '';
            gridHtml += `<div class="grid-input-cell ${endClass}"><input type="number" name="details[${index}].shipQuantities[${s.size}]" value="${shipQty}" min="0"></div>`;
        });

        // Đóng thẻ container và thêm các input ẩn
        gridHtml += '</div>';
        let variantIdInputs = sizes.map(s => `<input type="hidden" name="details[${index}].variantIds[${s.size}]" value="${s.variantId}">`).join('');

        container.innerHTML = gridHtml + variantIdInputs;
    };

    // HÃY THAY THẾ TOÀN BỘ HÀM renderExistingDetails CŨ BẰNG HÀM NÀY
    // HÃY THAY THẾ HÀM renderExistingDetails BẰNG PHIÊN BẢN NÀY
    const renderExistingDetails = async (details) => {
        if (!details || details.length === 0) return;

        // Tải danh sách sản phẩm một lần để không phải gọi lại trong vòng lặp
        const products = await fetchProducts();

        // Dùng for...of để xử lý tuần tự, tránh lỗi bất đồng bộ
        for (const detail of details) {
            const card = createDetailCard(); // Luôn tạo card mới với cấu trúc HTML đúng

            // Lấy các element cần thiết từ card vừa tạo
            const productSelect = card.querySelector('.product-select');
            const productNameInput = card.querySelector('.product-name');
            const colorSelect = card.querySelector('.color-select');
            const colorNameInput = card.querySelector('.color-name');
            const unitInput = card.querySelector('.unit-name');

            // 1. Populate và chọn đúng Product
            productSelect.innerHTML = '<option value="">-- Select --</option>';
            let selectedProductData = null;
            products.forEach(p => {
                const option = new Option(p.code, p.id);
                option.dataset.productName = p.name;
                option.dataset.unitName = p.unitName;
                productSelect.add(option);
                if (p.id == detail.productId) {
                    selectedProductData = p;
                }
            });

            // Điền dữ liệu cho Product
            if (selectedProductData) {
                productSelect.value = detail.productId;
                productNameInput.value = selectedProductData.name;
                unitInput.value = selectedProductData.unitName;
            }

            // 2. Populate và chọn đúng Color
            const colors = await fetchColors(detail.productId);
            colorSelect.innerHTML = '<option value="">-- Color --</option>';
            let selectedColorData = null;
            colors.forEach(c => {
                const option = new Option(c.colorCode, c.colorCode);
                option.dataset.colorName = c.colorName;
                colorSelect.add(option);
                if (c.colorCode == detail.color) {
                    selectedColorData = c;
                }
            });

            // Điền dữ liệu cho Color
            if (selectedColorData) {
                colorSelect.value = detail.color;
                colorNameInput.value = selectedColorData.colorName;
            }

            // Cập nhật các giá trị ẩn
            card.querySelector(`[name="details[${card.dataset.index}].productId"]`).value = detail.productId;
            card.querySelector(`[name="details[${card.dataset.index}].color"]`).value = detail.color;

            // 3. Xây dựng ma trận size với dữ liệu có sẵn
            await buildSizeMatrix(card, detail.productId, detail.color, detail);
        }
    };
    // === BẮT ĐẦU GỌI HÀM RENDER KHI TRANG TẢI ===
    const detailsDataEl = document.getElementById('details-data');
    if (detailsDataEl) {
        const existingDetails = JSON.parse(detailsDataEl.textContent);
        renderExistingDetails(existingDetails);
    }

    // --- Event Handlers ---
    addDetailBtn.addEventListener('click', async () => {
        const card = createDetailCard();
        const productSelect = card.querySelector('.product-select');
        const products = await fetchProducts();

        productSelect.innerHTML = '<option value="">-- Select --</option>';
        products.forEach(p => {
            const option = new Option(p.code, p.id);
            option.dataset.unitName = p.unitName;
            option.dataset.productName = p.name;
            productSelect.add(option);
        });
    });

    detailsContainer.addEventListener('change', async (e) => {
        const target = e.target;
        const card = target.closest('.detail-card');
        if (!card) return;

        if (target.classList.contains('product-select')) {
            const selectedOption = target.options[target.selectedIndex];
            card.querySelector('.product-name').value = selectedOption.dataset.productName || '';
            const colorSelect = card.querySelector('.color-select');
            card.querySelector('.color-name').value = '';
            card.querySelector('.unit-name').value = selectedOption.dataset.unitName || '';
            card.querySelector(`[name="details[${card.dataset.index}].productId"]`).value = target.value;

            if (target.value) {
                const colors = await fetchColors(target.value);
                colorSelect.innerHTML = '<option value="">-- Color --</option>';
                colors.forEach(c => {
                    const option = new Option(c.colorCode, c.colorCode);
                    option.dataset.colorName = c.colorName; // Lưu colorName vào data attribute
                    colorSelect.add(option);
                });
            } else {
                colorSelect.innerHTML = '<option value="">-- Select Product --</option>';
            }
            card.querySelector('.size-matrix-container').innerHTML = '';
        }

        if (target.classList.contains('color-select')) {
            const selectedOption = target.options[target.selectedIndex];
            card.querySelector('.color-name').value = selectedOption.dataset.colorName || '';

            const productId = card.querySelector('.product-select').value;
            card.querySelector(`[name="details[${card.dataset.index}].color"]`).value = target.value;
            buildSizeMatrix(card, productId, target.value, e.detail || null);
        }
    });

    detailsContainer.addEventListener('click', (e) => {
        if (e.target.closest('.delete-detail-btn')) {
            e.target.closest('.detail-card').remove();
        }
    });
});
