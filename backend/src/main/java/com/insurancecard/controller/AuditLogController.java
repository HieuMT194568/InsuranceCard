package com.insurancecard.controller;

import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.AuditLogDtos.AuditLogResponse;
import com.insurancecard.model.enums.AuditAction;
import com.insurancecard.service.AuditLogService;

/** M02 (Hiếu) – UC-25 Xem nhật ký hệ thống. Chỉ đọc, chỉ ADMIN. */
@RestController
@RequestMapping("/api/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public PageResponse<AuditLogResponse> search(@RequestParam(required = false) String q,
                                                 @RequestParam(required = false) AuditAction action,
                                                 @RequestParam(required = false) String entityType,
                                                 @RequestParam(required = false) LocalDate from,
                                                 @RequestParam(required = false) LocalDate to,
                                                 @RequestParam(required = false) Integer page,
                                                 @RequestParam(required = false) Integer size) {
        return auditLogService.search(q, action, entityType, from, to, page, size);
    }
}
