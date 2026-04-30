package com.supermarket.common;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
    
    public boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        if (rawPassword == null) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return encoder.matches(rawPassword, encodedPassword);
        }
        return rawPassword.equals(encodedPassword);
    }
}