package com.insurancecard.model.entity;

import com.insurancecard.model.enums.GarageStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** M02 – Gara sửa chữa liên kết. Biên bản giám định (M15) chọn gara từ danh mục này. */
@Getter
@Setter
@Entity
@Table(name = "garages")
public class Garage extends BaseEntity {

    @Column(name = "garage_code", nullable = false, length = 20)
    private String garageCode;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(nullable = false, length = 50)
    private String province;

    @Column(nullable = false, length = 10)
    private String phone;

    @Column(length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GarageStatus status;

    @Column(length = 500)
    private String note;
}
