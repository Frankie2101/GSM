document.addEventListener('DOMContentLoaded', function() {
    const addBtn = document.getElementById('addDetailBtn');
    const tableBody = document.getElementById('detailTableBody');
    const deleteSelectedBtn = document.getElementById('deleteSelectedBtn');
    const selectAllCheckbox = document.getElementById('selectAllCheckbox');
    const materialGroupTemplate = document.getElementById('material-groups-data-template');
    const numericInputSelector = 'input[data-name="usageValue"], input[data-name="waste"]';

    /**
     * An object to cache material lists (Fabric, Trim) to avoid redundant API calls.
     */
    const materialCache = {};

    /**
     * Re-calculates sequence numbers and updates the 'name' attribute of all form inputs in the table.
     * This is crucial for Spring MVC to correctly bind the list of details on form submission.
     * Example: details[0].rmType, details[1].rmType, etc.
     */
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

    /**
     * Fetches a list of materials (Fabric or Trim) from the API and populates a select dropdown.
     * Uses the materialCache to serve cached data if available.
     * @param {string} type - The material type ('FA' or 'TR').
     * @param {HTMLElement} selectElement - The <select> element to populate.
     * @param {string|null} selectedId - The ID of the item to be pre-selected.
     */
        const fetchMaterials = async (type, materialGroupId, selectElement, selectedId) => {
            if (!type || !materialGroupId) {
                selectElement.innerHTML = '<option value="">-- Select RM Type and Material Group --</option>';
                return;
            }

            const cacheKey = `${type}_${materialGroupId}`;
            if (materialCache[cacheKey]) {
                populateRmCodeSelect(selectElement, materialCache[cacheKey], selectedId);
                return;
            }

            try {
                const response = await fetch(`/api/materials?type=${type}&materialGroupId=${materialGroupId}`);
                if (!response.ok) throw new Error('Network response was not ok');
                const materials = await response.json();
                materialCache[cacheKey] = materials;
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
                if (selectedId && material.id.toString() === selectedId.toString()) {
                    option.selected = true;
                }
                selectElement.appendChild(option);
            });
            selectElement.dispatchEvent(new Event('change', {'bubbles': true}));
        };

        /**
         * Event listener for the 'Add Detail' button, which adds a new blank row to the table.
         */
        if (addBtn) {
            addBtn.addEventListener('click', function () {
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
                <td>
                    <select class="form-select material-group-select" data-name="materialGroupId">
                        ${materialGroupTemplate.innerHTML}
                    </select>
                </td>
                <td><select class="form-select rm-code-select" data-name="rmId"></select></td>
                <td><input type="text" class="form-control rm-name-input" readonly></td>
                <td><input type="text" class="form-control rm-unit-input" readonly></td>
                <td><input type="number" step="0.001" class="form-control" data-name="usageValue" value="0" required min="0" required></td>
                <td><input type="number" step="0.01" class="form-control" data-name="waste" value="0" required min="0" required></td>
                <td class="text-center align-middle">
                    <button type="button" class="btn btn-sm btn-outline-danger delete-row-btn"><i class="bi bi-trash"></i></button>
                </td>
            </tr>
        `;

                /**
                 * Event delegation for 'change' events within the table body.
                 * Handles the chained select logic:
                 * - When RM Type changes, it fetches the corresponding material codes.
                 * - When RM Code changes, it fetches that material's details (name, unit).
                 */
                if (tableBody) {
                    tableBody.insertAdjacentHTML('beforeend', newRowHtml);
                    reindexAndNameRows();
                }
            });
        }


        if (tableBody) {
            tableBody.addEventListener('change', async function (e) {
                const target = e.target;
                const row = target.closest('tr');
                if (!row) return;

                if (target.classList.contains('rm-type-select') || target.classList.contains('material-group-select')) {
                    const rmCodeSelect = row.querySelector('.rm-code-select');
                    row.querySelector('.rm-name-input').value = '';
                    row.querySelector('.rm-unit-input').value = '';

                    const type = row.querySelector('.rm-type-select').value;
                    const groupId = row.querySelector('.material-group-select').value;

                    fetchMaterials(type, groupId, rmCodeSelect, null);
                } else if (target.classList.contains('rm-code-select')) {
                    const rmNameInput = row.querySelector('.rm-name-input');
                    const rmUnitInput = row.querySelector('.rm-unit-input');

                    rmNameInput.value = '';
                    rmUnitInput.value = '';

                    const rmId = target.value;
                    const rmType = row.querySelector('.rm-type-select').value;

                    if (rmId && rmType) {
                        try {
                            const response = await fetch(`/api/material-details/${rmId}?type=${rmType}`);
                            if (!response.ok) throw new Error('Material details not found');
                            const details = await response.json();

                            rmNameInput.value = details.name || '';
                            rmUnitInput.value = details.unitName || '';
                        } catch (error) {
                            console.error('Failed to fetch material details:', error);
                        }
                    }
                }
            });

            tableBody.addEventListener('click', function (e) {
                if (e.target.closest('.delete-row-btn')) {
                    e.target.closest('tr').remove();
                    reindexAndNameRows();
                }
            });
        }

        if (deleteSelectedBtn) {
            deleteSelectedBtn.addEventListener('click', function () {
                if (!tableBody) return;
                const checkedBoxes = tableBody.querySelectorAll('.row-checkbox:checked');
                if (checkedBoxes.length === 0) {
                    Swal.fire({
                        icon: 'error',
                        title: 'No Details Selected',
                        text: 'Please select at least one detail to delete.'
                    });
                    return;
                }
                checkedBoxes.forEach(checkbox => checkbox.closest('tr').remove());
                reindexAndNameRows();
            });
        }

        if (selectAllCheckbox) {
            selectAllCheckbox.addEventListener('change', function () {
                if (!tableBody) return;
                tableBody.querySelectorAll('.row-checkbox').forEach(checkbox => {
                    checkbox.checked = this.checked;
                });
            });
        }

    /**
     * Loop through each existing detail row in the table on page load.
     * This function initializes the dynamic dropdowns for each pre-existing row.
     */
        document.querySelectorAll('#detailTableBody tr').forEach(row => {
            const groupSelect = row.querySelector('.material-group-select');
            const typeSelect = row.querySelector('.rm-type-select');
            const codeSelect = row.querySelector('.rm-code-select');

            groupSelect.innerHTML = materialGroupTemplate.innerHTML;
            const selectedGroupId = groupSelect.getAttribute('data-selected-id');
            if (selectedGroupId) {
                groupSelect.value = selectedGroupId;
            }

            const typeValue = typeSelect.value;
            const groupIdValue = groupSelect.value;
            const selectedRmId = codeSelect.getAttribute('data-selected-id');

            if (typeValue && groupIdValue) {
                fetchMaterials(typeValue, groupIdValue, codeSelect, selectedRmId);
            }
        });

    /**
     * Handles the 'keydown' event BEFORE a character is entered into the input field.
     * This proactively prevents the user from typing invalid characters (e.g., letters, multiple decimal points, negative signs).
     */
    document.addEventListener('keydown', function(event) {
        if (event.target.matches(numericInputSelector)) {
            const input = event.target;
            const key = event.key;

            if (event.ctrlKey || event.metaKey || key.length > 1) {
                return;
            }

            if (key === '.' && input.value.includes('.')) {
                event.preventDefault();
                return;
            }

            if (key === '-') {
                event.preventDefault();
                return;
            }

            if (!/[0-9.]/.test(key)) {
                event.preventDefault();
            }
        }
    });

    /**
     * Handles the 'input' event AFTER the value of the input has already changed.
     * This is a failsafe to sanitize the content, such as pasting text,
     * and to ensure the final value is not negative.
     */
    document.addEventListener('input', function(event) {
        if (event.target.matches(numericInputSelector)) {
            const input = event.target;
            if (parseFloat(input.value) < 0) {
                input.value = '';
            }
        }
    });

        reindexAndNameRows();
});