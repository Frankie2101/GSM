document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('addDetailBtn');
    const tableBody = document.getElementById('detailTableBody');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');

    const materialCache = { FA: null, TR: null };

    const reindexAndNameRows = () => {
        if (!tableBody) return;
        const rows = tableBody.querySelectorAll('tr');
        rows.forEach((row, index) => {
            const seqCell = row.querySelector('.seq-number');
            if (seqCell) seqCell.textContent = index + 1;

            const inputs = row.querySelectorAll('[data-name]');
            inputs.forEach(input => {
                const dataName = input.getAttribute('data-name');
                input.name = `details[${index}].${dataName}`;
                if (dataName === 'seq') {
                    input.value = index + 1;
                }
            });
        });
    };

    const fetchMaterials = async (type, selectElement, selectedId) => {
        if (!type) {
            selectElement.innerHTML = '<option value="">-- Select RM Type First --</option>';
            return;
        }
        if (materialCache[type]) {
            populateRmCodeSelect(selectElement, materialCache[type], selectedId);
            return;
        }
        try {
            const response = await fetch(`/api/materials?type=${type}`);
            if (!response.ok) throw new Error('Network response was not ok');
            const materials = await response.json();
            materialCache[type] = materials;
            populateRmCodeSelect(selectElement, materials, selectedId);
        } catch (error) {
            console.error('Failed to fetch materials:', error);
            selectElement.innerHTML = '<option value="">Error loading data</option>';
        }
    };

    const populateRmCodeSelect = (selectElement, materials, selectedId) => {
        selectElement.innerHTML = '<option value="">-- Select Code --</option>';
        materials.forEach(material => {
            const option = document.createElement('option');
            option.value = material.id;
            option.textContent = material.code;
            // Dữ liệu 'name' và 'unit' không có ở API này, sẽ được lấy ở một API khác
            if (selectedId && material.id.toString() === selectedId.toString()) {
                option.selected = true;
            }
            selectElement.appendChild(option);
        });
        // Kích hoạt sự kiện 'change' để xử lý cho các dòng đã có sẵn khi tải trang
        selectElement.dispatchEvent(new Event('change', { 'bubbles': true }));
    };

    if (addBtn) {
        addBtn.addEventListener('click', function() {
            const newRowHtml = `
            <tr>
                <td class="text-center align-middle">
                    <input class="form-check-input row-checkbox" type="checkbox">
                </td>
                <td class="align-middle text-center seq-number"></td>
                <input type="hidden" data-name="seq">
                <input type="hidden" data-name="bomTemplateDetailId" value="">
                <td>
                    <select class="form-select rm-type-select" data-name="rmType">
                        <option value="" selected>-- Select --</option>
                        <option value="FA">Fabric</option>
                        <option value="TR">Trim</option>
                    </select>
                </td>
                <td><select class="form-select rm-code-select" data-name="rmId"></select></td>
                <td><input type="text" class="form-control rm-name-input" readonly></td>
                <td><input type="text" class="form-control rm-unit-input" readonly></td>
                <td><input type="number" step="0.001" class="form-control" data-name="usageValue" required></td>
                <td><input type="number" step="0.01" class="form-control" data-name="waste" value="0" required></td>
                <td class="text-center align-middle">
                    <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn"><i class="bi bi-trash"></i></button>
                </td>
            </tr>
        `;
            if (tableBody) {
                tableBody.insertAdjacentHTML('beforeend', newRowHtml);
                reindexAndNameRows();
            }
        });
    }

    if (tableBody) {
        tableBody.addEventListener('change', async function(e) {
            const target = e.target;
            const row = target.closest('tr');
            if (!row) return;

            // Khi người dùng thay đổi RM Type
            if (target.classList.contains('rm-type-select')) {
                const rmCodeSelect = row.querySelector('.rm-code-select');
                row.querySelector('.rm-name-input').value = '';
                row.querySelector('.rm-unit-input').value = '';
                fetchMaterials(target.value, rmCodeSelect, null);
            }
            // Khi người dùng thay đổi RM Code
            else if (target.classList.contains('rm-code-select')) {
                const rmNameInput = row.querySelector('.rm-name-input');
                const rmUnitInput = row.querySelector('.rm-unit-input');

                // Luôn xóa dữ liệu cũ trước khi fetch
                rmNameInput.value = '';
                rmUnitInput.value = '';

                const rmId = target.value;
                const rmType = row.querySelector('.rm-type-select').value;

                if (rmId && rmType) {
                    try {
                        // **FIX: Gọi API thứ 2 để lấy chi tiết material**
                        const response = await fetch(`/api/material-details/${rmId}?type=${rmType}`);
                        if (!response.ok) throw new Error('Material details not found');
                        const details = await response.json();

                        // Cập nhật giá trị vào các ô input
                        rmNameInput.value = details.name || '';
                        rmUnitInput.value = details.unitName || '';
                    } catch (error) {
                        console.error('Failed to fetch material details:', error);
                    }
                }
            }
        });

        tableBody.addEventListener('click', function(e) {
            if (e.target.closest('.delete-row-btn')) {
                e.target.closest('tr').remove();
                reindexAndNameRows();
            }
        });
    }

    if (deleteSelectedBtn) {
        deleteSelectedBtn.addEventListener('click', function() {
            if (!tableBody) return;
            const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
            if (checkedBoxes.length === 0) {
                Swal.fire({ icon: 'error', title: 'No Details Selected', text: 'Please select at least one detail to delete.' });
                return;
            }
            checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
            reindexAndNameRows();
        });
    }

    if (selectAllCheckbox) {
        selectAllCheckbox.addEventListener('change', function() {
            if (!tableBody) return;
            tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                checkbox.checked = this.checked;
            });
        });
    }

    // Khởi tạo các dòng đã có sẵn khi tải trang
    document.querySelectorAll('.rm-type-select').forEach(select => {
        const row = select.closest('tr');
        const rmCodeSelect = row.querySelector('.rm-code-select');
        const selectedId = rmCodeSelect.getAttribute('data-selected-id');
        if (select.value) {
            fetchMaterials(select.value, rmCodeSelect, selectedId);
        }
    });

    reindexAndNameRows();
});