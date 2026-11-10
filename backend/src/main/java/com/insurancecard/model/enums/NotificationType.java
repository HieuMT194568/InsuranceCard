package com.insurancecard.model.enums;

/** Loại thông báo trong ứng dụng – mỗi loại ứng với 1 sự kiện ở mục 10.2 của SRS. */
public enum NotificationType {
    CONTRACT_ACTIVATED,
    CONTRACT_EXPIRING,
    CONTRACT_CANCELLED,
    CERTIFICATE_ISSUED,
    PENALTY_ISSUED,
    ACCIDENT_RESOLVED,
    CLAIM_UPDATED,
    TICKET_REPLIED,
    SYSTEM
}
