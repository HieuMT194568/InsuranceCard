package com.insurancecard.model.enums;

/**
 * Vai trò người dùng. Phân cấp quyền: ADMIN &gt; STAFF (xem SecurityConfig#roleHierarchy) – mọi API
 * {@code hasRole('STAFF')} tự cho ADMIN dùng. CUSTOMER chỉ thao tác trên dữ liệu của mình.
 */
public enum Role {
    CUSTOMER, STAFF, ADMIN
}
