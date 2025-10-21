package com.gsm.service;

import com.gsm.dto.UserDto;
import java.util.List;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * Defines the contract for user management business operations.
 * It also extends UserDetailsService to integrate with Spring Security.
 */
public interface UserService extends UserDetailsService {

    /**
     * Searches for users based on a keyword (e.g., username, phone number, department).
     * @param keyword The search term.
     * @return A list of UserDto objects matching the search criteria.
     */
    List<UserDto> search(String keyword);

    /**
     * Finds a specific user by their unique identifier.
     * @param id The ID of the user to find.
     * @return The UserDto object if found.
     * @throws com.gsm.exception.ResourceNotFoundException if the user with the given ID does not exist.
     */
    UserDto findById(Long id);

    /**
     * Saves a user (either creates a new one or updates an existing one).
     * @param userDto The UserDto object containing the data to save.
     * @return The saved UserDto object, potentially with updated ID or audit fields.
     * @throws com.gsm.exception.DuplicateResourceException if a user with the same phone number already exists.
     */
    UserDto save(UserDto userDto);

    /**
     * Sets the active status (active or inactive) for a specific user.
     * @param id The ID of the user to update.
     * @param status The desired active status (true for active, false for inactive).
     * @throws com.gsm.exception.ResourceNotFoundException if the user with the given ID does not exist.
     */
    void setActiveFlag(Long id, boolean status);
}