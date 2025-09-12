package com.gsm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

// LƯU Ý: ĐÂY CHỈ LÀ CONTROLLER TẠM THỜI, SẼ XÓA SAU KHI LẤY ĐƯỢC MẬT KHẨU
@RestController
public class TempUtilController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/encode/{password}")
    public String encodePassword(@PathVariable String password) {
        return passwordEncoder.encode(password);
    }
}