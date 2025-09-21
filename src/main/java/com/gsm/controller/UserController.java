package com.gsm.controller;

import com.gsm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * @param model The Spring Model to pass data to the view.
     * @param request The HttpServletRequest to access the CSRF token.
     * @return The name of the user list view template.
     */
    @GetMapping
    public String showUserList(Model model, HttpServletRequest request) {
        model.addAttribute("isUserPage", true);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "user/user_list";
    }

    /**
     * An API endpoint to get a list of distinct Departments and Production Lines.
     * This is used to populate dropdowns or autocomplete fields on the frontend.
     * @return A ResponseEntity containing a map with lists of departments and production lines.
     */
    @GetMapping("/production-info")
    @ResponseBody
    public ResponseEntity<Map<String, List<String>>> getProductionInfo() {
        Map<String, List<String>> data = new HashMap<>();
        data.put("departments", userRepository.findDistinctDepartments());
        data.put("productionLines", userRepository.findDistinctProductionLines());
        return ResponseEntity.ok(data);
    }
}