package com.gsm.service;

import com.gsm.dto.ProductionOutputDto;
import java.time.LocalDate;
import java.util.List;

/**
 * Service layer interface for managing Production Output business logic.
 */
public interface ProductionOutputService {

    /**
     * Retrieves all production output records.
     * @return A list of all {@link ProductionOutputDto}s.
     */
    List<ProductionOutputDto> findAll();

    /**
     * Searches for production output records based on multiple criteria.
     * @param keyword Search term for Sale Order number or style.
     * @param outputDateFrom The start date of the date range filter.
     * @param outputDateTo The end date of the date range filter.
     * @param department The department to filter by.
     * @param productionLine The production line to filter by.
     * @return A list of matching {@link ProductionOutputDto}s.
     */
    List<ProductionOutputDto> search(String keyword, LocalDate outputDateFrom, LocalDate outputDateTo, String department, String productionLine);

    /**
     * Deletes one or more production output records by their IDs.
     * @param ids A list of IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Saves a new or updates an existing production output record.
     * @param dto The DTO containing the data to save.
     * @return The saved {@link ProductionOutputDto}.
     */
    ProductionOutputDto save(ProductionOutputDto dto);

    /**
     * Finds a single production output record by its ID.
     * @param id The ID of the record to find.
     * @return The found {@link ProductionOutputDto}.
     */
    ProductionOutputDto findById(Long id);
}