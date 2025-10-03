package com.gsm.service;

import com.gsm.dto.OrderBOMDto;
import com.gsm.dto.PurchaseOrderDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service layer interface for managing Purchase Order business logic.
 * Defines the contract for all PO operations, including state transitions like approval and rejection.
 */
public interface PurchaseOrderService {

    /**
     * Generates new Purchase Orders based on the details of a saved Order BOM.
     * @param bomDto The saved OrderBOMDto containing items to be purchased. / DTO của OrderBOM đã lưu, chứa các mặt hàng cần mua.
     * @return A map containing a summary message and a list of new PO numbers.
     */
    Map<String, Object> generatePOsFromOrderBOM(OrderBOMDto bomDto);

    /**
     * Retrieves a list of all Purchase Orders, optimized for list view.
     * @return A list of {@link PurchaseOrderDto} objects.
     */
    List<PurchaseOrderDto> findAll();

    /**
     * Finds a single Purchase Order by its ID, with full details.
     * @param id The ID of the PO to find.
     * @return The detailed {@link PurchaseOrderDto}.
     */
    PurchaseOrderDto findById(Long id);

    /**
     * Saves a new PO or updates an existing one based on the provided DTO.
     * This is the primary method for persisting PO data from the UI.
     * @param dto The DTO containing the PO data.
     * @return The saved {@link PurchaseOrderDto}.
     */
    PurchaseOrderDto save(PurchaseOrderDto dto);


    /**
     * Deletes one or more Purchase Orders based on a list of IDs.
     * REFACTORED: Changed from deleteById to handle bulk deletion consistently.
     * @param ids A list of PO IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Submits a Purchase Order for approval, changing its status from 'New' to 'Submitted'.
     * @param id The ID of the PO to submit.
     */
    void submitForApproval(Long id);

    /**
     * Finds all Purchase Orders that are currently awaiting approval (status is 'Submitted').
     * @return A list of pending {@link PurchaseOrderDto}s.
     */
    List<PurchaseOrderDto> findPendingApproval();

    /**
     * Approves a Purchase Order, changing its status to 'Approved'.
     * @param id The ID of the PO to approve.
     */
    void approve(Long id);

    /**
     * Rejects a Purchase Order, changing its status to 'Rejected'.
     * @param id The ID of the PO to reject.
     */
    void reject(Long id);
}