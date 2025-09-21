package com.gsm.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for serving the main HTML page for the Production Output feature.
 * The page itself is a shell, and all data is loaded asynchronously by its corresponding JavaScript.
 */
@Controller
@RequestMapping("/production-outputs")
public class ProductionOutputController {

    /**
     * Displays the main page for viewing and managing production outputs.
     * @param model Model to pass the CSRF token to the view for JavaScript to use.
     * @return The path to the production output list view template.
     */
    @GetMapping
    public String showProductionOutputPage(Model model, HttpServletRequest request) {
        model.addAttribute("isProductionOutputPage", true);
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "production-output/production_output_list";
    }
}