package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.NotificationDtos.NotificationResponse;
import com.insurancecard.model.enums.NotificationType;

/**
 * M03 – Thông báo trong ứng dụng. Các module gọi {@link #notify} tại các sự kiện ở mục 10.2 của SRS.
 */
public interface NotificationService {

    /**
     * Tạo thông báo cho 1 người dùng. Không bao giờ làm hỏng nghiệp vụ gọi nó: tự cắt độ dài,
     * bỏ qua nếu người nhận không tồn tại hoặc đã có thông báo cùng refKey.
     *
     * @param link   route frontend, ví dụ "/customer/contracts/15" (có thể null)
     * @param refKey khoá chống trùng, ví dụ "CONTRACT_EXPIRING:15" (null = không chống trùng)
     */
    void notify(Long userId, NotificationType type, String title, String content, String link, String refKey);

    PageResponse<NotificationResponse> listMine(boolean unreadOnly, Integer page, Integer size);

    long countUnreadMine();

    NotificationResponse markRead(Long id);

    int markAllRead();

    /** UC-28: nhắc các hợp đồng sắp hết hạn, trả về số thông báo mới tạo. */
    int remindExpiringContracts();
}
