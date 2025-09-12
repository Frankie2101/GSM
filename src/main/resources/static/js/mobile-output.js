// --- Phan khoi tao khi trang da tai xong ---
document.addEventListener('DOMContentLoaded', () => {

    // --- Phan khai bao bien ---
    const form = document.getElementById('outputForm');
    const saleOrderNoInput = document.getElementById('saleOrderNo');
    const styleSelect = document.getElementById('style');
    const colorSelect = document.getElementById('color');
    const quantityInput = document.getElementById('outputQuantity');
    const departmentInput = document.getElementById('department');
    const productionLineInput = document.getElementById('productionLine');
    const csrfTokenInput = document.getElementById('csrfToken');
    const csrfHeaderName = 'X-CSRF-TOKEN';
    const csrfToken = csrfTokenInput ? csrfTokenInput.value : '';
    let saleOrderDetailsCache = [];
    let currentUser = null;
    const loadingOverlay = document.getElementById('loading-overlay');
    const toast = document.getElementById('toast');
    const toastContent = document.getElementById('toast-content');
    let toastTimeout;

    // --- Ham xu ly giao dien ---
    const showLoading = () => loadingOverlay.classList.remove('hidden');
    const hideLoading = () => loadingOverlay.classList.add('hidden');

    function showToast(message, isSuccess = true) {
        clearTimeout(toastTimeout);
        const icon = isSuccess
            ? `<div class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-green-500 bg-green-100 rounded-lg"><svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"></path></svg></div>`
            : `<div class="inline-flex items-center justify-center flex-shrink-0 w-8 h-8 text-red-500 bg-red-100 rounded-lg"><svg class="w-5 h-5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"></path></svg></div>`;

        toastContent.innerHTML = `${icon}<div class="ml-3 text-sm font-normal">${message}</div>`;
        toast.classList.add('show');

        toastTimeout = setTimeout(() => { toast.classList.remove('show'); }, 3000);
    }

    function populateSelect(select, options, placeholder) {
        select.innerHTML = `<option value="">${placeholder}</option>`;
        options.forEach(opt => {
            const el = document.createElement('option');
            el.value = opt;
            el.textContent = opt;
            select.appendChild(el);
        });
    }

    // --- Cac ham logic ---
    async function loadCurrentUser() {
        if (currentUser) {
            departmentInput.value = currentUser.department || '';
            productionLineInput.value = currentUser.productionLine || '';
            return;
        }
        try {
            const response = await fetch('/api/users/me');
            if (response.ok) {
                currentUser = await response.json();
                departmentInput.value = currentUser.department || '';
                productionLineInput.value = currentUser.productionLine || '';
            } else {
                showToast('Could not load user info.', false);
            }
        } catch (error) {
            console.error('Error fetching current user:', error);
        }
    }

    async function handleSaleOrderNoChange() {
        const soNo = saleOrderNoInput.value.trim();
        styleSelect.innerHTML = '<option value="">Style</option>';
        colorSelect.innerHTML = '<option value="">Color</option>';

        if (!soNo) return;
        showLoading();

        try {
            const response = await fetch(`/api/sale-orders/${soNo}/details`);
            if (!response.ok) throw new Error('Sale Order not found or invalid.');

            const details = await response.json(); // Day la luc loi JSON.parse co the xay ra
            saleOrderDetailsCache = details;

            const styles = [...new Set(details.map(d => d.style))];
            populateSelect(styleSelect, styles, 'Select Style');

            if (styles.length === 1) {
                styleSelect.value = styles[0];
                handleStyleChange();
            }
        } catch (error) {
            showToast(error.message, false);
        } finally {
            hideLoading();
        }
    }

    function handleStyleChange() {
        const selectedStyle = styleSelect.value;
        colorSelect.innerHTML = '<option value="">Color</option>';
        if (!selectedStyle) return;

        const colors = saleOrderDetailsCache
            .filter(d => d.style === selectedStyle)
            .map(d => d.color);
        const uniqueColors = [...new Set(colors)];
        populateSelect(colorSelect, uniqueColors, 'Select Color');
        if (uniqueColors.length === 1) {
            colorSelect.value = uniqueColors[0];
        }
    }

    async function handleSubmit(event) {
        event.preventDefault();
        if (!form.checkValidity()) {
            showToast("Please fill in all required fields.", false);
            return;
        }

        showLoading();
        if (navigator.vibrate) navigator.vibrate(50);

        try {
            const response = await fetch('/api/production-outputs/save', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json', [csrfHeaderName]: csrfToken },
                body: JSON.stringify({
                    saleOrderNo: saleOrderNoInput.value,
                    style: styleSelect.value,
                    color: colorSelect.value,
                    outputQuantity: quantityInput.value,
                    department: departmentInput.value,
                    productionLine: productionLineInput.value,
                    outputDate: new Date().toISOString().split('T')[0]
                })
            });

            if (response.ok) {
                showToast("Report saved successfully!");
                saleOrderNoInput.value = '';
                quantityInput.value = '';
                styleSelect.innerHTML = '<option value="">Style</option>';
                colorSelect.innerHTML = '<option value="">Color</option>';
                saleOrderNoInput.focus();
            } else {
                const error = await response.json();
                throw new Error(error.message || 'Failed to save report.');
            }
        } catch (error) {
            showToast(error.message, false);
        } finally {
            hideLoading();
        }
    }

    // --- Gan su kien ---
    form.addEventListener('submit', handleSubmit);
    saleOrderNoInput.addEventListener('blur', handleSaleOrderNoChange);
    styleSelect.addEventListener('change', handleStyleChange);

    // --- Khoi chay ---
    loadCurrentUser();
});

// --- Dang ky Service Worker (nam ngoai event DOMContentLoaded) ---
if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('/service-worker.js')
            .then(registration => console.log('ServiceWorker registration successful'))
            .catch(err => console.log('ServiceWorker registration failed: ', err));
    });
}
