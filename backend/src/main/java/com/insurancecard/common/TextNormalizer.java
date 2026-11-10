package com.insurancecard.common;

import java.util.Locale;

/** Chuẩn hoá chuỗi nhập vào trước khi lưu / so sánh. */
public final class TextNormalizer {

    private TextNormalizer() {}

    /** Email: bỏ khoảng trắng hai đầu, chuyển chữ thường. */
    public static String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Chuẩn hoá họ tên: gộp khoảng trắng thừa. */
    public static String normalizeName(String name) {
        return name == null ? null : name.trim().replaceAll("\s+", " ");
    }
}
