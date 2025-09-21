package com.gsm.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A simple controller to handle requests to the root URL ("/").
 */
@Controller
public class HomeController {

    /**
     * Redirects requests from the root URL ("/") to the main login page ("/login").
     * This ensures that users accessing the base domain are immediately sent to the login screen.
     * @return A redirect view name.
     */
    @GetMapping("/")
    public String redirectToLogin() {
        return "redirect:/login";
    }
}