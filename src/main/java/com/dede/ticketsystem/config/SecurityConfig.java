package com.dede.ticketsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF để demo cho nhanh, production nên bật
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/dang-nhap", "/dang-ky", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll()
                        .anyRequest().permitAll())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.permitAll());

        return http.build();
    }
}
