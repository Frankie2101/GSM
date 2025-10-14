package com.gsm.controller.advice;

import com.gsm.model.User;
import com.gsm.repository.UserRepository;
import com.gsm.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import com.gsm.enums.Permission;
import java.util.HashMap;
import java.util.Map;
import com.gsm.enums.UserType;


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

    /**
     * Creates a map of permissions for the currently logged-in user.
     * This map is used in templates (like the sidebar) to conditionally show/hide elements.
     * An Admin user will have all permissions set to true.
     * @return A Map where keys are permission names (e.g., "USER_MANAGEMENT") and values are booleans.
     */
    @ModelAttribute("userPermissions")
    public Map<String, Boolean> addUserPermissionsToModel() {
        Map<String, Boolean> permissions = new HashMap<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Fetch the full User object directly from the database.
            String username = authentication.getName();
            User user = userRepository.findByUserName(username).orElse(null);

            if (user != null) {
                // Use a robust, case-insensitive check directly on the User's UserType.
                boolean isAdmin = (user.getUserType() != null && "Admin".equalsIgnoreCase(user.getUserType().name()));

                // Iterate through all possible permissions defined in the enum.
                for (Permission p : Permission.values()) {
                    if (isAdmin) {
                        // If user is Admin, grant all permissions.
                        permissions.put(p.name(), true);
                    } else {
                        // For Normal users, check their specific permissions stored in the database.
                        boolean hasPermission = user.getPermissions().contains(p.name());
                        permissions.put(p.name(), hasPermission);
                    }
                }
            }
        }
        return permissions;
    }
    }
