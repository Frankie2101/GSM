// In file: src/main/java/com/gsm/controller/UserController.java

package com.gsm.controller;

import com.gsm.enums.Permission;
import com.gsm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.web.csrf.CsrfToken;
import javax.servlet.http.HttpServletRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Handles web requests related to the User management pages.
 */
@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    /**
     * Displays the user list page.
     * Requires USER_VIEW permission.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('USER_VIEW')")
    public String showUserList(Model model, HttpServletRequest request) {
        model.addAttribute("isUserPage", true);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        // Group all permissions from the Enum by their module name (e.g., "User Management").
        // We use a LinkedHashMap to maintain a predictable order.
        Map<String, List<Permission>> grouped = Arrays.stream(Permission.values())
                .collect(Collectors.groupingBy(p -> p.getDisplayName().split(": ")[0], LinkedHashMap::new, Collectors.toList()));

        // Transform the map into a list of objects that Mustache can easily loop through.
        List<Map<String, Object>> permissionGroups = new ArrayList<>();
        grouped.forEach((groupName, permissions) -> {
            Map<String, Object> groupMap = new HashMap<>();
            groupMap.put("groupName", groupName);
            // For each permission, create a map with its technical name and display name (the action part).
            groupMap.put("permissions", permissions.stream().map(p -> Map.of(
                    "name", p.name(),
                    "actionName", p.getDisplayName().split(": ")[1]
            )).collect(Collectors.toList()));
            permissionGroups.add(groupMap);
        });

        model.addAttribute("permissionGroups", permissionGroups);

        return "user/user_list";
    }

    /**
     * An API endpoint to get a list of distinct Departments and Production Lines.
     * Requires USER_VIEW permission.
     */
    @GetMapping("/production-info")
    @ResponseBody
    @PreAuthorize("hasAuthority('ROLE_Admin') or hasAuthority('USER_VIEW')")
    public ResponseEntity<Map<String, List<String>>> getProductionInfo() {
        Map<String, List<String>> data = new HashMap<>();
        data.put("departments", userRepository.findDistinctDepartments());
        data.put("productionLines", userRepository.findDistinctProductionLines());
        return ResponseEntity.ok(data);
    }
}