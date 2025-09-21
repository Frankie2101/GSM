/**
 * @fileoverview Manages the highly dynamic UI for the Sale Order form.
 * See technical explanation above for core concepts like Data Hydration, Caching, and Map Binding.
 */
document.addEventListener('DOMContentLoaded', function() {
    const detailsContainer = document.getElementById('detailsContainer');
    const addDetailBtn = document.getElementById('addDetailBtn');
    const customerSelect = document.getElementById('customerId');
    const orderNoInput = document.getElementById('saleOrderNo');
    const currencyInput = document.getElementById('currencyCode');
    const orderDateInput = document.getElementById('orderDate');
    let detailIndex = 0;

    if (!document.querySelector('[name="saleOrderId"]').value) {
        const today = new Date().toISOString().split('T')[0];
        orderDateInput.value = today;
    }

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


    /**
     * Creates the main HTML structure for a new detail card.
     * The card includes dropdowns for product/color and a container for the size matrix.
     * @returns {HTMLElement} The newly created card element.
     */
        const createDetailCard = () => {
        const card = document.createElement('div');
        card.className = 'card mb-2 detail-card';
        card.dataset.index = detailIndex++;
        card.innerHTML = `
        <div class="card-body">
            <div class="d-flex align-items-end">

                <div class="d-flex align-items-end" style="flex: 0 0 50%;">
                    <div style="width: 25%;" class="me-2">
                        <label class="form-label form-label-sm mb-0">Product</label>
                        <select class="form-select form-select-sm product-select" title="Product"></select>
                    </div>
                    <div style="width: 30%;" class="me-2">
                        <label class="form-label form-label-sm mb-0">Product Name</label>
                        <input type="text" class="form-control form-control-sm product-name" readonly title="Product Name">
                    </div>
                    <div style="width: 20%;" class="me-2">
                        <label class="form-label form-label-sm mb-0">Color</label>
                        <select class="form-select form-select-sm color-select" title="Color"></select>
                    </div>
                    <div style="width: 10%;" class="me-2">
                         <label class="form-label form-label-sm mb-0">Unit</label>
                         <input type="text" class="form-control form-control-sm unit-name" readonly title="Unit">
                    </div>
                </div>

                <div class="table-responsive size-matrix-container" style="flex: 0 0 50%;">
                    </div>

                <div class="ps-2">
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

    /**
     * Asynchronously builds and renders a size matrix table within a detail card.
     * This function dynamically creates table headers for sizes and input rows for quantities/prices.
     * It uses a naming convention (`details[index].quantities[size]`) to enable
     * data binding to Map structures in the Spring Boot backend.
     * @param {HTMLElement} card - The parent card element where the table will be rendered.
     * @param {string} productId - The selected product's ID.
     * @param {string} color - The selected color code.
     * @param {object|null} existingDetail - Pre-existing data for this card (used in edit mode).
     */
        const buildSizeMatrix = async (card, productId, color, existingDetail = null) => {
        const sizes = await fetchSizes(productId, color);
        const container = card.querySelector('.size-matrix-container');
        const index = card.dataset.index;

        if (!sizes || sizes.length === 0) {
            container.innerHTML = '<small class="text-muted">No sizes for this color.</small>';
            return;
        }

        // 1. Create table headers from the list of sizes.
        let tableHeaders = sizes.map(s => `<th>${s.size}</th>`).join('');

        // 2. Create input rows for Order Qty, Price, etc.
        let orderQtyInputs = sizes.map(s => {
            const qty = existingDetail?.quantities?.[s.size] || '';
            return `<td><input type="number" class="form-control form-control-sm text-center" name="details[${index}].quantities[${s.size}]" value="${qty}" min="0"></td>`;
        }).join('');

        let priceInputs = sizes.map(s => {
            const price = existingDetail?.prices?.[s.size] ?? s.price;
            return `<td><input type="number" step="0.01" class="form-control form-control-sm text-center" name="details[${index}].prices[${s.size}]" value="${price || ''}"></td>`;
        }).join('');

        let shipQtyInputs = sizes.map(s => {
            const shipQty = existingDetail?.shipQuantities?.[s.size] || '';
            return `<td><input type="number" class="form-control form-control-sm text-center" name="details[${index}].shipQuantities[${s.size}]" value="${shipQty}" min="0"></td>`;
        }).join('');

        // 3. Assemble the full table HTML.
        const tableHtml = `
        <table class="table table-bordered table-sm">
            <thead class="table-light">
                <tr>
                    <th style="width: 100px;"></th> ${tableHeaders}
                </tr>
            </thead>
            <tbody>
                <tr>
                    <th>Order Qty</th>
                    ${orderQtyInputs}
                </tr>
                <tr>
                    <th>Price</th>
                    ${priceInputs}
                </tr>
                <tr>
                    <th>Ship Qty</th>
                    ${shipQtyInputs}
                </tr>
            </tbody>
        </table>
    `;

        // 4. Create hidden inputs for variant IDs, also for Map binding.
        const variantIdInputs = sizes.map(s => `<input type="hidden" name="details[${index}].variantIds[${s.size}]" value="${s.variantId}">`).join('');

        container.innerHTML = tableHtml + variantIdInputs;
    };

    /**
     * Renders the initial set of detail cards when editing an existing sale order.
     * It uses a `for...of` loop to correctly handle the `async/await` calls in sequence.
     * @param {Array<object>} details - The list of detail DTOs from the backend's JSON data island.
     */
    const renderExistingDetails = async (details) => {
        if (!details || details.length === 0) return;

        const products = await fetchProducts();

        for (const detail of details) {
            const card = createDetailCard();

            const productSelect = card.querySelector('.product-select');
            const productNameInput = card.querySelector('.product-name');
            const colorSelect = card.querySelector('.color-select');
            const unitInput = card.querySelector('.unit-name');

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

            if (selectedProductData) {
                productSelect.value = detail.productId;
                productNameInput.value = selectedProductData.name;
                unitInput.value = selectedProductData.unitName;
            }

            const colors = await fetchColors(detail.productId);
            colorSelect.innerHTML = '<option value="">-- Color --</option>';
            colors.forEach(c => {
                const option = new Option(c.colorCode, c.colorCode);
                colorSelect.add(option);
            });
            colorSelect.value = detail.color;

            card.querySelector(`[name="details[${card.dataset.index}].productId"]`).value = detail.productId;
            card.querySelector(`[name="details[${card.dataset.index}].color"]`).value = detail.color;
            await buildSizeMatrix(card, detail.productId, detail.color, detail);
        }
    };

    // This block is the entry point for "edit" mode.
    const detailsDataEl = document.getElementById('details-data');
    if (detailsDataEl) {
        // 1. Parse the JSON string from the <script> tag into a JavaScript object.
        const existingDetails = JSON.parse(detailsDataEl.textContent);
        // 2. Call the main rendering function to build the UI from this data.
        renderExistingDetails(existingDetails);
    }

    // --- Event Handlers ---

    /**
     * Handles the click on the "Add Product" button.
     * It creates a new blank card and populates its product dropdown with all available products.
     */
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

    /**
     * This is the master event listener for the entire details container.
     * It uses event delegation to efficiently handle changes on dynamically created elements.
     */
    detailsContainer.addEventListener('change', async (e) => {
        const target = e.target;
        const card = target.closest('.detail-card');
        if (!card) return;

        // --- Logic when a PRODUCT is selected ---
        if (target.classList.contains('product-select')) {
            // 1. Get extra data stored in the selected option's data-* attributes.
            const selectedOption = target.options[target.selectedIndex];

            // 2. Auto-fill other fields like product name and unit.
            card.querySelector('.product-name').value = selectedOption.dataset.productName || '';

            // 3. Clear the old color/size info.
            const colorSelect = card.querySelector('.color-select');
            card.querySelector('.unit-name').value = selectedOption.dataset.unitName || '';
            card.querySelector(`[name="details[${card.dataset.index}].productId"]`).value = target.value;

            // 4. If a valid product was selected, fetch and populate its colors.
            if (target.value) {
                const colors = await fetchColors(target.value);
                colorSelect.innerHTML = '<option value="">-- Color --</option>';
                colors.forEach(c => {
                    const option = new Option(c.colorCode, c.colorCode);
                    colorSelect.add(option);
                });
            } else {
                colorSelect.innerHTML = '<option value="">-- Select Product --</option>';
            }
            card.querySelector('.size-matrix-container').innerHTML = '';
        }

        // --- Logic when a COLOR is selected ---
        if (target.classList.contains('color-select')) {
            const selectedOption = target.options[target.selectedIndex];
            const productId = card.querySelector('.product-select').value;
            card.querySelector(`[name="details[${card.dataset.index}].color"]`).value = target.value;

            // Call the main function to build the size matrix for the selected product and color.
            buildSizeMatrix(card, productId, target.value, e.detail || null);
        }
    });

    /**
     * Uses event delegation to handle clicks on the delete button of any detail card.
     */
    detailsContainer.addEventListener('click', (e) => {
        // If the clicked element (or its parent) is a delete button...
        if (e.target.closest('.delete-detail-btn')) {
            // ...find its parent card and remove it from the DOM.
            e.target.closest('.detail-card').remove();
        }
    });
});
