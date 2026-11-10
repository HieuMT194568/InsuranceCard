package com.insurancecard.model.dto;

import java.time.LocalDateTime;

import com.insurancecard.model.enums.GarageStatus;
import com.insurancecard.validation.ValidationRules;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** M02 – Gara liên kết (UC-43, UC-44). */
public final class GarageDtos {

    private GarageDtos() {}

    public record GarageRequest(
            @NotBlank(message = "Vui lòng nhập mã gara")
            @Size(max = 20, message = "Mã gara tối đa 20 ký tự")
            String garageCode,

            @NotBlank(message = "Vui lòng nhập tên gara")
            @Size(min = 2, max = 150, message = "Tên gara từ 2 đến 150 ký tự")
            String name,

            @NotBlank(message = "Vui lòng nhập địa chỉ")
            @Size(min = 5, max = 255, message = "Địa chỉ từ 5 đến 255 ký tự")
            String address,

            @NotBlank(message = "Vui lòng nhập tỉnh / thành phố")
            @Size(min = 2, max = 50, message = "Tỉnh / thành phố từ 2 đến 50 ký tự")
            String province,

            @NotBlank(message = "Vui lòng nhập số điện thoại")
            @Pattern(regexp = ValidationRules.PHONE, message = ValidationRules.PHONE_MSG)
            String phone,

            @Size(max = 100, message = "Email tối đa 100 ký tự")
            @Pattern(regexp = ValidationRules.EMAIL, message = ValidationRules.EMAIL_MSG)
            String email,

            @NotNull(message = "Vui lòng chọn trạng thái")
            GarageStatus status,

            @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
            String note) {}

    public record GarageResponse(
            Long id,
            String garageCode,
            String name,
            String address,
            String province,
            String phone,
            String email,
            GarageStatus status,
            String note,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {}

    /** Dữ liệu công khai cho khách vãng lai (không có ghi chú nội bộ). */
    public record PublicGarageResponse(
            Long id,
            String garageCode,
            String name,
            String address,
            String province,
            String phone,
            String email) {}
}
