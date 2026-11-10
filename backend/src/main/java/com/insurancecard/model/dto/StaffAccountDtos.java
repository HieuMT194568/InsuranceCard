package com.insurancecard.model.dto;

import java.time.LocalDateTime;

import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;
import com.insurancecard.validation.ValidationRules;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** M02 – Quản lý tài khoản nhân viên / quản trị viên (UC-24). */
public final class StaffAccountDtos {

    private StaffAccountDtos() {}

    public record StaffAccountCreateRequest(
            @NotBlank(message = "Vui lòng nhập họ tên")
            @Size(min = 2, max = 100, message = "Họ tên từ 2 đến 100 ký tự")
            @Pattern(regexp = ValidationRules.FULL_NAME, message = ValidationRules.FULL_NAME_MSG)
            String fullName,

            @NotBlank(message = "Vui lòng nhập email")
            @Size(max = 100, message = "Email tối đa 100 ký tự")
            @Pattern(regexp = ValidationRules.EMAIL, message = ValidationRules.EMAIL_MSG)
            String email,

            @NotBlank(message = "Vui lòng nhập số điện thoại")
            @Pattern(regexp = ValidationRules.PHONE, message = ValidationRules.PHONE_MSG)
            String phone,

            @NotNull(message = "Vui lòng chọn vai trò")
            Role role) {}

    /** Không sửa email (định danh đăng nhập). Khoá / mở khoá qua trường status. */
    public record StaffAccountUpdateRequest(
            @NotBlank(message = "Vui lòng nhập họ tên")
            @Size(min = 2, max = 100, message = "Họ tên từ 2 đến 100 ký tự")
            @Pattern(regexp = ValidationRules.FULL_NAME, message = ValidationRules.FULL_NAME_MSG)
            String fullName,

            @NotBlank(message = "Vui lòng nhập số điện thoại")
            @Pattern(regexp = ValidationRules.PHONE, message = ValidationRules.PHONE_MSG)
            String phone,

            @NotNull(message = "Vui lòng chọn vai trò")
            Role role,

            @NotNull(message = "Vui lòng chọn trạng thái")
            UserStatus status) {}

    public record StaffAccountResponse(
            Long id,
            String email,
            String fullName,
            String phone,
            Role role,
            UserStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    /** Mật khẩu tạm chỉ trả về đúng 1 lần khi tạo tài khoản. */
    public record StaffAccountCreatedResponse(StaffAccountResponse account, String temporaryPassword) {}
}
