package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

/**
 * Defines the contract for business operations related to the Order BOM.
 */
public interface OrderBOMService {
    @Transactional(readOnly = true)

    /**
     * Retrieves all Order BOMs, typically for display in a list view.
     * Marked as read-only transaction.
     * @return A list of OrderBOMDto objects, potentially simplified for list display.
     */
    List<OrderBOMDto> findAll();

    /**
     * Finds an existing Order BOM by its associated Sale Order ID.
     * If no BOM exists for the given Sale Order, it creates a new, empty OrderBOMDto shell.
     * @param saleOrderId The ID of the Sale Order.
     * @return An existing OrderBOMDto (including details) or a new, empty OrderBOMDto linked to the Sale Order.
     * @throws com.gsm.exception.ResourceNotFoundException if the Sale Order with the given ID does not exist when creating a new BOM.
     */
    OrderBOMDto findOrCreateBySaleOrderId(Long saleOrderId);

    /**
     * Generates a preview of what the Order BOM details would look like if based on a specific BOM Template.
     * This is used to populate the form when a user selects a template, before saving.
     * @param saleOrderId The ID of the target Sale Order (used for calculating quantities).
     * @param bomTemplateId The ID of the BOM Template to use as a base.
     * @return An OrderBOMDto populated with details derived from the template, but not yet saved.
     * @throws com.gsm.exception.ResourceNotFoundException if either the Sale Order or BOM Template is not found.
     */
    OrderBOMDto generatePreviewFromTemplate(Long saleOrderId, Long bomTemplateId);

    /**
     * Saves an Order BOM (either creates a new one or updates an existing one), including its details.
     * This method implements logic to synchronize the detail lines intelligently.
     * @param dto The OrderBOMDto object containing the data to save.
     * @return The saved OrderBOMDto object, potentially with updated IDs or audit fields.
     * @throws com.gsm.exception.ResourceNotFoundException if related entities (SaleOrder, BOMTemplate, Fabric, Trim) are not found.
     */
    OrderBOMDto save(OrderBOMDto dto);

    /**
     * Saves the Order BOM and then automatically generates Purchase Orders (POs)
     * based on the calculated purchase quantities for items not already included in a PO.
     * @param bomDtoFromForm The OrderBOMDto submitted from the form, containing potential purchase quantities.
     * @return A map containing a success message or potentially other result information.
     * @throws IllegalStateException if no valid new items are found to generate POs.
     * @throws com.gsm.exception.ResourceNotFoundException if related entities cannot be found during PO creation.
     */
    Map<String, Object> saveAndGeneratePOs(OrderBOMDto bomDtoFromForm);
}