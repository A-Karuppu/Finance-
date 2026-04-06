package com.finance.dashboard.config;

import com.finance.dashboard.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Enables @PreAuthorize on controllers
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()

                // Dashboard — VIEWER, ANALYST, ADMIN can read
                .requestMatchers(HttpMethod.GET, "/api/dashboard/**")
                    .hasAnyRole("VIEWER", "ANALYST", "ADMIN")

                // Records — GET allowed for ANALYST and ADMIN
                .requestMatchers(HttpMethod.GET, "/api/records/**")
                    .hasAnyRole("ANALYST", "ADMIN")

                // Records — write operations only for ADMIN
                .requestMatchers(HttpMethod.POST, "/api/records/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/records/**")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/records/**")
                    .hasRole("ADMIN")

                // User management — ADMIN only
                .requestMatchers("/api/users/**").hasRole("ADMIN")

                // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
