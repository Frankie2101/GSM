package com.gsm.service;

import com.gsm.dto.BOMTemplateDto;
import java.util.List;

/**
 * Defines the contract for business operations related to BOM Templates.
 * Provides methods for finding, saving, deleting, and searching BOM templates.
 */
public interface BOMTemplateService {

    /**
     * Retrieves all BOM templates, typically for display in a list view.
     * @return A list of BOMTemplateDto objects, potentially simplified for list display.
     */
    List<BOMTemplateDto> findAll();

    /**
     * Finds a specific BOM template by its unique identifier, including its details.
     * @param id The ID of the BOM template to find.
     * @return The BOMTemplateDto object if found.
     * @throws com.gsm.exception.ResourceNotFoundException if the template with the given ID does not exist.
     */
    BOMTemplateDto findById(Long id);

    /**
     * Saves a BOM template (either creates a new one or updates an existing one).
     * @param dto The BOMTemplateDto object containing the data to save.
     * @return The saved BOMTemplateDto object, potentially with updated ID or audit fields.
     * @throws com.gsm.exception.DuplicateResourceException if a template with the same code already exists.
     */
    BOMTemplateDto save(BOMTemplateDto dto);

    /**
     * Deletes multiple BOM templates based on a list of their IDs.
     * @param ids A list of IDs of the BOM templates to delete.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Searches for BOM templates based on a keyword (e.g., code or name).
     * @param keyword The search term.
     * @return A list of BOMTemplateDto objects matching the search criteria.
     */
    List<BOMTemplateDto> search(String keyword);
}
