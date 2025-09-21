package com.gsm.controller;

import org.springframework.http.HttpHeaders; // Thêm import này
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * A controller specifically for handling Zalo's domain verification process.
 */
@Controller
public class DomainVerificationController {

    /**
     * Serves a specific HTML file required by Zalo to verify domain ownership.
     * It returns raw HTML content with a verification meta tag.
     * It also includes a special header to bypass ngrok browser warnings.
     * @return A ResponseEntity containing the HTML content and custom headers.
     */
    @GetMapping(value = "/zalo-platform-site-verification.html", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> zaloDomainVerifier() {
        String verificationCode = "IzIT8ihM52KTnRKiuxHW5Yd2vGx5j39KD3Sp";
        String htmlContent = String.format(
                "<!DOCTYPE html><html><head><meta name=\"zalo-platform-site-verification\" content=\"%s\" /></head><body></body></html>",
                verificationCode
        );

        // Add a header to request that Ngrok skips its interstitial warning page.
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.set("ngrok-skip-browser-warning", "true");

        return ResponseEntity.ok()
                .headers(responseHeaders)
                .body(htmlContent);
    }
}