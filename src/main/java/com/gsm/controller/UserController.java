package com.gsm.controller;

import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping
    public String showUserList(Model model, HttpServletRequest request) {
        model.addAttribute("isUserPage", true); // Để highlight sidebar
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "user/user_list";
    }
}