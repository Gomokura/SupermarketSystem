package com.supermarket.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * PasswordEncoder - 密码加密与校验工具类
 * 使用 BCrypt 算法加密密码，支持兼容旧系统的明文密码
 */
public class PasswordEncoder {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     */
    public static String encode(String rawPassword) {
        if (rawPassword == null) return null;
        return encoder.encode(rawPassword);
    }

    /**
     * 校验密码
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) return false;
        if (rawPassword == null) return false;
        // BCrypt 格式
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return encoder.matches(rawPassword, encodedPassword);
        }
        // 兼容明文密码
        return rawPassword.equals(encodedPassword);
    }
}
