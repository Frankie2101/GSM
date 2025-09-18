package com.gsm.service;

import com.gsm.dto.SaleOrderDto;
import java.util.List;

/**
 * Service layer interface for managing Sale Order-related business logic.
 * Defines the contract for all operations that can be performed on sale orders.
 */
public interface SaleOrderService {

    /**
     * Retrieves a list of all sale orders, simplified for list view display.
     * @return A list of {@link SaleOrderDto} objects.
     */
    List<SaleOrderDto> findAll();

    /**
     * Finds a single sale order by its unique identifier, with full details.
     * @param id The ID of the sale order to find.
     * @return The {@link SaleOrderDto} with full details, including line items.
     */
    SaleOrderDto findById(Long id);

    /**
     * Saves a new sale order or updates an existing one.
     * @param dto The DTO containing the sale order data to be saved.
     * @return The saved {@link SaleOrderDto}, including any database-generated values.
     */
    SaleOrderDto save(SaleOrderDto dto);

    /**
     * Deletes one or more sale orders based on a list of IDs.
     * @param ids A list of sale order IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Searches for sale orders based on a keyword.
     * @param keyword The term to search for (e.g., in sale order number or customer name).
     * @return A list of {@link SaleOrderDto}s that match the keyword.
     */
    List<SaleOrderDto> search(String keyword);

    /**
     * Finds a single sale order by its unique sale order number.
     * @param saleOrderNo The unique number of the sale order.
     * @return The {@link SaleOrderDto} (simplified version) matching the number.
     */
    SaleOrderDto findBySaleOrderNo(String saleOrderNo);

}