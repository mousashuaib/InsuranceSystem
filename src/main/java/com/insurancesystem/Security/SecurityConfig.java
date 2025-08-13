package com.insurancesystem.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity // لتفعيل @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth

                        // السماح المفتوح لتسجيل الدخول والتسجيل
                        .requestMatchers("/api/auth/**").permitAll()

                        // مدير التأمين فقط
                        .requestMatchers("/api/policies/**").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/coverages/**").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/enrollments/**").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/roles/**").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/clients/role-requests/**").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/clients/*/reject").hasRole("INSURANCE_MANAGER")
                        .requestMatchers("/api/clients/*/role-requests/approve").hasRole("INSURANCE_MANAGER")

                        // مدير الطوارئ لو عنده Endpoints خاصة ممكن إضافتها هنا
                        // .requestMatchers("/api/emergency-manager/**").hasRole("EMERGENCY_MANAGER")

                        // المستخدم العادي (العميل)
                        .requestMatchers("/api/clients/**").hasRole("INSURANCE_CLIENT")

                        // أي شيء غير مصرح به → رفض مباشر
                        .anyRequest().denyAll()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
}
