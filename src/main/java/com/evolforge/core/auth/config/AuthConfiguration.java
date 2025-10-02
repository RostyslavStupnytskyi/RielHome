package com.evolforge.core.auth.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableConfigurationProperties(AuthProperties.class)
public class AuthConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder(AuthProperties properties) {
        String encoder = properties.getPassword().getEncoder();
        if ("bcrypt".equalsIgnoreCase(encoder)) {
            return new BCryptPasswordEncoder();
        }
        // Default to Argon2id with balanced settings for general-purpose hosts.
        return new Argon2PasswordEncoder(16, 32, 1, 1 << 12, 3);
    }
}
