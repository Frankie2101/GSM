package com.gsm.controller.api;

import com.gsm.dto.UserDto;
import com.gsm.security.CustomUserDetails;
import com.gsm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for handling all API requests related to User management.
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @Autowired private UserService userService;

    /**
     * Searches for users based on a keyword.
     * @param keyword The search term.
     * @return A list of UserDtos matching the criteria.
     */
    @GetMapping
    public List<UserDto> searchUsers(@RequestParam(required = false) String keyword) {
        return userService.search(keyword);
    }

    /**
     * Retrieves a single user by their ID.
     * @param id The ID of the user.
     * @return A ResponseEntity containing the UserDto.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        UserDto userDto = userService.findById(id);
        return ResponseEntity.ok(userDto);
    }

    /**
     * Retrieves information about the currently authenticated user.
     * @param userDetails The CustomUserDetails object injected by Spring Security.
     * @return A ResponseEntity containing the current user's DTO.
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUserInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        UserDto userDto = userService.findById(userDetails.getUserId());
        return ResponseEntity.ok(userDto);
    }

    /**
     * Saves a user (creates a new one or updates an existing one).
     * @param userDto The user data sent in the request body.
     * @return A ResponseEntity with the saved UserDto.
     */
    @PostMapping
    public ResponseEntity<UserDto> saveUser(@Valid @RequestBody UserDto userDto) {
        UserDto savedUser = userService.save(userDto);
        return ResponseEntity.ok(savedUser);
    }

    /**
     * Deactivates a user by setting their active flag to false.
     * @param id The ID of the user to deactivate.
     * @return An empty ResponseEntity with a 200 OK status.
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.setActiveFlag(id, false);
        return ResponseEntity.ok().build();
    }

    /**
     * Activates a user by setting their active flag to true.
     * @param id The ID of the user to activate.
     * @return An empty ResponseEntity with a 200 OK status.
     */
    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.setActiveFlag(id, true);
        return ResponseEntity.ok().build();
    }
}