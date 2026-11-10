package com.insurancecard.service;

import java.time.LocalDate;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.AuditLogDtos.AuditLogResponse;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.AuditAction;

/**
 * M02 – Nhật ký hệ thống. Mọi module gọi {@link #record} tại các sự kiện ở mục 10.3 của SRS
 * (ghi trong cùng transaction nghiệp vụ, tự lấy người đang đăng nhập và IP).
 */
public interface AuditLogService {

    /** Ghi nhật ký cho người đang đăng nhập (null nếu hệ thống tự chạy). */
    void record(AuditAction action, String entityType, Long entityId, String description);

    /** Ghi nhật ký với người thực hiện chỉ định (dùng khi chưa có phiên đăng nhập, ví dụ lúc login). */
    void record(User actor, AuditAction action, String entityType, Long entityId, String description);

    PageResponse<AuditLogResponse> search(String q, AuditAction action, String entityType, LocalDate from,
                                          LocalDate to, Integer page, Integer size);
}
