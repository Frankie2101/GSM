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
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

/**
 * Configures the application's security settings using Spring Security.
 * This class defines multiple ordered SecurityFilterChains to handle different
 * parts of the application (Zalo API, Mobile App, Web App) with distinct security rules.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String REMEMBER_ME_KEY = "2s55ZeKq18c9xB0Ts2tQ";

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico");
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    /**
     * [PRIORITY 0 - HIGHEST]
     * This filter chain is ONLY for Zalo's domain verification file.
     * It has the highest priority to ensure it's processed first.
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
     * [PRIORITY 1]
     * This filter chain is for ALL Zalo Mini App APIs.
     * It permits all requests under /api/zalo/** and disables CSRF.
     */
    @Bean
    @Order(1)
    public SecurityFilterChain zaloApiFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/api/zalo/**")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    /**
     * [PRIORITY 2]
     * This filter chain is for the mobile application (PWA).
     * It handles authentication and access for the /mobile-* interface.
     */
    @Bean
    @Order(2)
    public SecurityFilterChain mobileSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .antMatcher("/mobile-**")
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // Allow public access to the login page and PWA assets.
                        .antMatchers("/mobile-login", "/manifest.json", "/service-worker.js", "/icons/**").permitAll()
                        // All other requests starting with /mobile-** must be authenticated.
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
     * [PRIORITY 3 - LOWEST]
     * The default filter chain for the internal web application (GSM).
     * This acts as a catch-all for any requests not matched by higher-priority chains.
     */
    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/dashboard", true)
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

