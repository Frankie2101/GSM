package com.gsm.config;

import com.gsm.security.CustomUserDetails; // THÊM IMPORT NÀY
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<Long> auditorProvider() {
        return () -> {
            // Lấy thông tin xác thực từ Spring Security
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Kiểm tra xem người dùng đã đăng nhập hay chưa
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.empty(); // Trả về empty nếu chưa đăng nhập
            }

            // Lấy principal và ép kiểu về CustomUserDetails
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Trả về UserId
            return Optional.of(userDetails.getUserId());
        };
    }
}