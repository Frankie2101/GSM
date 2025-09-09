package com.gsm.controller;

import org.springframework.security.web.csrf.CsrfToken; // THÊM IMPORT NÀY
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest; // THÊM IMPORT NÀY

@Controller
public class AuthController {

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model,
                            HttpServletRequest request) { // THÊM HttpServletRequest VÀO THAM SỐ

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "You have been logged out successfully.");
        }

        // **DÒNG QUAN TRỌNG: Thêm CSRF token vào model để view có thể sử dụng**
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));

        return "login/login";
    }
}