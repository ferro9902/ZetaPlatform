package com.aruba.zeta.userauth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Security configuration for user-auth-service.
 *
 * Spring Security does not auto-create a PasswordEncoder bean;
 * it must be defined explicitly so it can be injected into UserAuthServiceImpl.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
