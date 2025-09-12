package com.gsm.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/production-outputs")
public class ProductionOutputController {

    /**
     * Endpoint chính: Trả về trang HTML ban đầu.
     * Mọi dữ liệu sẽ được load thông qua các lời gọi API từ JavaScript.
     */
    @GetMapping
    public String showProductionOutputPage(Model model, HttpServletRequest request) {
        model.addAttribute("isProductionOutputPage", true); // Để highlight sidebar
        // Gửi CSRF token qua model để JavaScript có thể sử dụng
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "production-output/production_output_list";
    }
}