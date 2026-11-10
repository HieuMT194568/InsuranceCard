package com.insurancecard.common;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Sinh mật khẩu tạm 10 ký tự đúng chính sách mật khẩu (có hoa, thường, số, ký tự đặc biệt). */
public final class TemporaryPassword {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnpqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "@#$%&*!";

    private TemporaryPassword() {}

    public static String generate() {
        String all = UPPER + LOWER + DIGITS;
        List<Character> chars = new ArrayList<>();
        chars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        chars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        chars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        chars.add(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        for (int i = 0; i < 6; i++) {
            chars.add(all.charAt(RANDOM.nextInt(all.length())));
        }
        Collections.shuffle(chars, RANDOM);
        StringBuilder sb = new StringBuilder();
        chars.forEach(sb::append);
        return sb.toString();
    }
}
