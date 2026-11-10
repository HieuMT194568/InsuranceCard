package com.insurancecard.model.dto;

import java.time.LocalDateTime;

import com.insurancecard.model.enums.NotificationType;

/** M03 – Thông báo (UC-26). */
public final class NotificationDtos {

    private NotificationDtos() {}

    public record NotificationResponse(
            Long id,
            NotificationType type,
            String title,
            String content,
            String link,
            boolean read,
            LocalDateTime readAt,
            LocalDateTime createdAt) {}

    public record UnreadCountResponse(long count) {}
}
