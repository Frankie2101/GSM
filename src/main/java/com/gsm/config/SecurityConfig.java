package com.gsm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Lớp cấu hình chính cho Spring Security.
 * Nơi định nghĩa các quy tắc bảo mật, trang đăng nhập, mã hóa mật khẩu...
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Bean này định nghĩa một bộ mã hóa mật khẩu.
     * BCrypt là thuật toán mã hóa một chiều mạnh mẽ và là tiêu chuẩn hiện nay.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean này định nghĩa cách Spring Security sẽ tìm kiếm thông tin người dùng.
     * TẠM THỜI: Chúng ta đang tạo một người dùng "in-memory" (chỉ tồn tại khi ứng dụng chạy).
     * SAU NÀY: Chúng ta sẽ thay thế nó bằng một dịch vụ kết nối vào database để lấy người dùng thật.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails user = User.builder()
                .username("user")
                .password(passwordEncoder().encode("password")) // Mật khẩu là "password"
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    /**
     * Bean này định nghĩa chuỗi bộ lọc bảo mật, là nơi áp dụng các quy tắc chính.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập không cần đăng nhập vào các tài nguyên tĩnh
                        .antMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // CHO PHÉP TRUY CẬP MỌI TRANG để tiện cho việc phát triển
                        .anyRequest().permitAll()
                )
                // Cấu hình form đăng nhập (sẽ dùng đến sau này)
                .formLogin(form -> form
                        .loginPage("/login") // Chỉ định URL của trang đăng nhập tùy chỉnh
                        .permitAll()
                )
                // Cấu hình chức năng đăng xuất
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );
        return http.build();
    }
}