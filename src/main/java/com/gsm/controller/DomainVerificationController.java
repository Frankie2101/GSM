package com.gsm.controller;

import org.springframework.http.HttpHeaders; // Thêm import này
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DomainVerificationController {

    @GetMapping(value = "/zalo-platform-site-verification.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> zaloDomainVerifier() {
        String verificationCode = "IzIT8ihM52KTnRKiuxHW5Yd2vGx5j39KD3Sp";
        String htmlContent = String.format(
                "<!DOCTYPE html><html><head><meta name=\"zalo-platform-site-verification\" content=\"%s\" /></head><body></body></html>",
                verificationCode
        );

        // MỚI: Thêm header để yêu cầu Ngrok bỏ qua trang cảnh báo
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("ngrok-skip-browser-warning", "true");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(htmlContent);
    }
}