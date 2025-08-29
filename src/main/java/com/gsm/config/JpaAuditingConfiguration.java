package com.gsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider") // Active JPA Auditing
public class JpaAuditingConfiguration {


    @Bean
    public AuditorAware<Long> auditorProvider() {
        // Tạm thời trả về 1L. Trong dự án thực tế, bạn sẽ lấy ID của user đã đăng nhập
        // từ Spring Security Context.
        return () -> {
            /*
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }
            // Giả sử Principal của bạn là một đối tượng UserDetails có chứa ID
            // YourUserDetailsClass userDetails = (YourUserDetailsClass) authentication.getPrincipal();
            // return Optional.of(userDetails.getId());
            */
            return Optional.of(1L); // << TẠM THỜI HARDCODE, SẼ CẬP NHẬT KHI CÓ LOGIN
        };
    }
}