package com.gsm.controller.advice;

import com.gsm.model.User;
import com.gsm.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private UserRepository userRepository;

    @ModelAttribute("loggedInUser")
    public User addUserInfoToModel() {
        // Lấy thông tin xác thực của người dùng hiện tại
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            // Lấy username từ Principal của Spring Security
            String username = authentication.getName();

            // Tìm kiếm người dùng đầy đủ trong database để lấy thêm thông tin (như Department)
            return userRepository.findByUserName(username).orElse(null);
        }

        return null;
    }


}