package com.winforrd.taskflow.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 加密密码
     *
     * @param password
     * @return
     */
    public static String encode(String password) {
        return encoder.encode(password);
    }

    /**
     * 验证密码
     *
     * @param password
     * @param encodedPassword
     * @return
     */
    public static boolean matches(String password, String encodedPassword) {
        return encoder.matches(password, encodedPassword);
    }
}
