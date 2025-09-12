package com.gsm.controller;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller này chịu trách nhiệm phục vụ các trang HTML
 * dành riêng cho giao diện di động (PWA).
 */
@Controller
public class MobileController {

    /**
     * Phục vụ trang nhập liệu chính cho di động.
     * Nếu người dùng chưa đăng nhập, Spring Security sẽ tự động chuyển hướng
     * họ đến trang login được định nghĩa trong SecurityConfig.
     * @param model Model để truyền dữ liệu tới view.
     * @param request HttpServletRequest để lấy CSRF token.
     * @return Tên của template view.
     */
    @GetMapping("/mobile-output")
    public String showMobileInputPage(Model model, HttpServletRequest request) {
        // Gửi CSRF token để Javascript có thể thực hiện các request POST tới API
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "mobile/mobile_output"; // Trỏ tới file: templates/mobile/input.mustache
    }

    /**
     * Phục vụ trang đăng nhập cho di động.
     * @param model Model để truyền dữ liệu tới view.
     * @param request HttpServletRequest để lấy CSRF token.
     * @return Tên của template view.
     */
    @GetMapping("/mobile-login")
    public String showMobileLoginPage(Model model, HttpServletRequest request) {
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "mobile/mobile_login"; // Trỏ tới file: templates/mobile/login.mustache
    }
}
