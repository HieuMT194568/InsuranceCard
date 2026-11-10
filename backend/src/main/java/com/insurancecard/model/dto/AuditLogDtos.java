package com.insurancecard.model.dto;

import java.time.LocalDateTime;

import com.insurancecard.model.enums.AuditAction;

/** M02 – Nhật ký hệ thống (UC-25). */
public final class AuditLogDtos {

    private AuditLogDtos() {}

    public record AuditLogResponse(
            Long id,
            AuditAction action,
            String entityType,
            Long entityId,
            String description,
            String ipAddress,
            Long userId,
            String userEmail,
            String userFullName,
            LocalDateTime createdAt) {}
}
