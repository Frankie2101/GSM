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
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String REMEMBER_ME_KEY = "2s55ZeKq18c9xB0Ts2tQ";


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * [ƯU TIÊN 0 - CAO NHẤT]
     * Chuỗi bộ lọc này CHỈ dùng để Zalo xác thực domain. (GIỮ NGUYÊN)
     */
    @Bean
    @Order(0)
    public SecurityFilterChain verificationFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/zalo-platform-site-verification.html")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * [ƯU TIÊN 1]
     * Chuỗi bộ lọc cho TẤT CẢ các API của Zalo Mini App. (GIỮ NGUYÊN)
     */
    @Bean
    @Order(1)
    public SecurityFilterChain zaloApiFilterChain(HttpSecurity http) throws Exception {
        http
                // SỬA LẠI: Dùng antMatcher() cho Spring Boot 2.7
                .antMatcher("/api/zalo/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * [ƯU TIÊN 2 - MỚI]
     * Chuỗi bộ lọc cho ứng dụng di động (PWA).
     * Xử lý đăng nhập và truy cập cho giao diện /mobile-*.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain mobileSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/mobile-**")
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // === THAY ĐỔI QUAN TRỌNG ===
                        // Thêm "/mobile-login" vào danh sách cho phép truy cập công khai.
                        .antMatchers("/mobile-login", "/manifest.json", "/service-worker.js", "/icons/**").permitAll()
                        // Tất cả các request khác bắt đầu bằng /mobile-** đều phải được xác thực
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/mobile-login")
                        .loginProcessingUrl("/mobile-login")
                        .defaultSuccessUrl("/mobile-output", true)
                        .failureUrl("/mobile-login?error=true")
                        .permitAll()
                )
                .rememberMe(rememberMe -> rememberMe
                        .key(REMEMBER_ME_KEY)
                        .tokenValiditySeconds(12 * 60 * 60)
                        .userDetailsService(userService)
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/mobile-login?logout=true")
                        .deleteCookies("JSESSIONID", "remember-me")
                )
                .csrf(csrf -> csrf
                        .ignoringAntMatchers("/api/**")
                );
        return http.build();
    }

    /**
     * [ƯU TIÊN 3 - THẤP NHẤT] - Đã đổi Order từ 2 -> 3
     * Chuỗi bộ lọc mặc định cho ứng dụng web nội bộ (GSM). (GIỮ NGUYÊN LOGIC)
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .antMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Trang login cho web desktop
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/sale-orders", true)
                        .failureUrl("/login?error=true")
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringAntMatchers("/api/**")
                );
        return http.build();
    }
}

