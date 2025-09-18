// File: src/main/java/com/gsm/service/FabricService.java
package com.gsm.service;

import com.gsm.dto.FabricDto;
import java.util.List;

/**
 * Service layer interface for managing Fabric-related business logic.
 * This defines the contract for all fabric operations, abstracting the implementation
 * from the controllers.
 */
public interface FabricService {
    /**
     * Retrieves a list of all fabrics.
     * @return A list of {@link FabricDto} objects.
     */
    List<FabricDto> findAll();

    /**
     * Finds a single fabric by its unique identifier.
     * @param id The ID of the fabric to find.
     * @return The {@link FabricDto} corresponding to the given ID.
     */
    FabricDto findById(Long id);

    /**
     * Saves a new fabric or updates an existing one.
     * @param fabricDto The DTO containing the fabric data.
     * @return The saved {@link FabricDto}.
     */
    FabricDto save(FabricDto fabricDto);

    /**
     * Deletes one or more fabrics by their IDs.
     * @param ids A list of fabric IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Searches for fabrics based on a keyword.
     * @param keyword The term to search for in fabric code and name.
     * @return A list of {@link FabricDto}s that match the keyword.
     */
    List<FabricDto> search(String keyword);
}