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

@Controller
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showUserList(Model model, HttpServletRequest request) {
        model.addAttribute("isUserPage", true); // Để highlight sidebar
        model.addAttribute("_csrf", request.getAttribute(CsrfToken.class.getName()));
        return "user/user_list";
    }

    // YÊU CẦU MỚI: API để lấy danh sách Department và Production Line
    @GetMapping("/production-info")
    @ResponseBody
    public ResponseEntity<Map<String, List<String>>> getProductionInfo() {
        Map<String, List<String>> data = new HashMap<>();
        data.put("departments", userRepository.findDistinctDepartments());
        data.put("productionLines", userRepository.findDistinctProductionLines());
        return ResponseEntity.ok(data);
    }
}