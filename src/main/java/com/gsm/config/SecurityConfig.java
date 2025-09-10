package com.gsm.config;

import com.gsm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * [ƯU TIÊN 0 - CAO NHẤT]
     * Chuỗi bộ lọc này CHỈ dùng để Zalo xác thực domain.
     * Nó cho phép truy cập công khai vào một URL duy nhất.
     */
    @Bean
    @Order(0)
    public SecurityFilterChain verificationFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/zalo-platform-site-verification.html") // Chỉ áp dụng cho URL này
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * [ƯU TIÊN 1]
     * Chuỗi bộ lọc cho TẤT CẢ các API của Zalo Mini App.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain zaloApiFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/api/zalo/**") // Áp dụng cho tất cả các đường dẫn bắt đầu bằng /api/zalo/
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());

        return http.build();
    }

    /**
     * [ƯU TIÊN 2 - THẤP NHẤT]
     * Chuỗi bộ lọc mặc định cho ứng dụng web nội bộ (GSM).
     */
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/sale-orders", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                // SỬA ĐỔI PHẦN LOGOUT
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // Cho phép GET request
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );
        return http.build();
    }
}

