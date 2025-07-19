package com.landmark.hlss_java.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class VerificationCodeGenerator {
    private static final String CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 避免易混淆字符
    private final Random random = new Random();

    public String generate() {
        return generate(6); // 默认6位验证码
    }

    public String generate(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }
}
