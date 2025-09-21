package com.gsm.controller.advice;

import com.gsm.model.User;
import com.gsm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * A @ControllerAdvice class to provide globally available methods and data for all controllers.
 * This is useful for adding common attributes to the model for every request.
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    /**
     * Adds the currently logged-in User object to the model for every request.
     * This method runs before controller methods and makes the "loggedInUser" object
     * available in all view templates.
     * @return The full User object for the authenticated user, or null if not authenticated.
     */
    @ModelAttribute("loggedInUser")
    public User addUserInfoToModel() {
        // Get the current user's authentication information from Spring Security.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Get the username from Spring Security's Principal.
            String username = authentication.getName();

            // Fetch the full User entity from the database to get additional info (like Department).
            return userRepository.findByUserName(username).orElse(null);
        }

        return null;
    }


}