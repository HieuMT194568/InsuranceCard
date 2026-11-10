package com.insurancecard.mapper;

import com.insurancecard.model.dto.NotificationDtos.NotificationResponse;
import com.insurancecard.model.entity.Notification;

/** M03 (Hiếu) – entity -> DTO cho thông báo. */
public final class NotificationMapper {

    private NotificationMapper() {}

    public static NotificationResponse notification(Notification n) {
        return new NotificationResponse(n.getId(), n.getType(), n.getTitle(), n.getContent(), n.getLink(),
                n.isRead(), n.getReadAt(), n.getCreatedAt());
    }
}
