package com.gsm.service;

import com.gsm.dto.TrimDto;
import java.util.List;

/**
 * Service layer interface for managing Trim-related business logic.
 * This defines the public contract for all trim operations, abstracting the implementation
 * from the controllers.
 */
public interface TrimService {

    /**
     * Retrieves a list of all trims.
     * @return A list of {@link TrimDto} objects, simplified for list view.
     */
    List<TrimDto> findAll();

    /**
     * Finds a single trim by its unique identifier.
     * @param id The ID of the trim to find.
     * @return The {@link TrimDto} with full details.
     * @throws com.gsm.exception.ResourceNotFoundException if no trim is found with the given ID.
     */
    TrimDto findById(Long id);

    /**
     * Saves a new trim or updates an existing one.
     * @param trimDto The DTO containing the trim data to be saved.
     * @return The saved {@link TrimDto}, including any database-generated values like IDs.
     */
    TrimDto save(TrimDto trimDto);

    /**
     * Deletes one or more trims based on a list of IDs.
     * @param ids A list of trim IDs to be deleted.
     */
    void deleteByIds(List<Long> ids);

    /**
     * Searches for trims based on a keyword.
     * @param keyword The term to search for in trim code and name.
     * @return A list of {@link TrimDto}s that match the keyword.
     */
    List<TrimDto> search(String keyword);
}