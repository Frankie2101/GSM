package com.gsm.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller is responsible for serving the HTML pages
 * dedicated to the mobile interface (PWA).
 */
@Controller
public class MobileController {

    /**
     * Serves the main data input page for the mobile interface.
     * If the user is not logged in, Spring Security will automatically redirect
     * them to the login page defined in SecurityConfig.
     * @param model Model to pass data to the view.
     * @param request HttpServletRequest to retrieve the CSRF token.
     * @return The name of the view template.
     */
    @GetMapping("/mobile-output")
    public String showMobileInputPage(Model model, HttpServletRequest request) {
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "mobile/mobile_output";
    }

    /**
     * Serves the login page for the mobile interface.
     * @param model Model to pass data to the view.
     * @param request HttpServletRequest to retrieve the CSRF token.
     * @return The name of the view template.
     */
    @GetMapping("/mobile-login")
    public String showMobileLoginPage(Model model, HttpServletRequest request) {
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "mobile/mobile_login";
    }
}
