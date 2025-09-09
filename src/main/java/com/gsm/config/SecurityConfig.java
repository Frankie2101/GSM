package com.gsm.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import com.gsm.service.UserService; // SỬA IMPORT NÀY

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private UserService userService; // SỬ DỤNG UserService
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // SỬ DỤNG userService ở đây
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * SỬA LỖI GỐC: Tạo một chuỗi bộ lọc bảo mật RIÊNG cho Zalo API.
     * @Order(1) đảm bảo quy tắc này được ưu tiên áp dụng trước.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain zaloApiFilterChain(HttpSecurity http) throws Exception {
        http
                // Áp dụng quy tắc này cho tất cả các đường dẫn bắt đầu bằng /api/zalo/
                .antMatcher("/api/zalo/**")
                .authorizeHttpRequests(auth -> auth
                        // Cho phép tất cả các yêu cầu này đi qua mà không cần xác thực
                        .anyRequest().permitAll()
                )
                // Yêu cầu Spring Security KHÔNG tạo session cho các API này (quan trọng)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Tắt tính năng chống tấn công CSRF cho các API này
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * Chuỗi bộ lọc bảo mật cho phần còn lại của ứng dụng web (ERP nội bộ).
     * @Order(2) đảm bảo quy tắc này được áp dụng sau quy tắc cho Zalo API.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        // THÊM authenticationProvider() VÀO ĐÂY ĐỂ KÍCH HOẠT
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                // Cấu hình form đăng nhập cho hệ thống web nội bộ
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang đăng nhập
                        .loginProcessingUrl("/login") // URL xử lý đăng nhập
                        .defaultSuccessUrl("/sale-orders", true) // Chuyển hướng sau khi thành công
                        .failureUrl("/login?error=true") // Chuyển hướng khi thất bại
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi đăng xuất
                        .permitAll()
                );

        return http.build();
    }

}