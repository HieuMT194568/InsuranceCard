package com.insurancecard.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.mapper.AdminMapper;
import com.insurancecard.model.dto.AuditLogDtos.AuditLogResponse;
import com.insurancecard.model.entity.AuditLog;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.AuditAction;
import com.insurancecard.repository.AuditLogRepository;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.AppUserPrincipal;
import com.insurancecard.service.AuditLogService;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private static final int MAX_DESCRIPTION = 500;
    private static final long MAX_RANGE_DAYS = 366;

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void record(AuditAction action, String entityType, Long entityId, String description) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User actor = auth != null && auth.getPrincipal() instanceof AppUserPrincipal me
                ? userRepository.getReferenceById(me.userId())
                : null;
        record(actor, action, entityType, entityId, description);
    }

    @Override
    @Transactional
    public void record(User actor, AuditAction action, String entityType, Long entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUser(actor);
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        String text = description == null ? "" : description;
        log.setDescription(text.length() > MAX_DESCRIPTION ? text.substring(0, MAX_DESCRIPTION) : text);
        log.setIpAddress(clientIp());
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(String q, AuditAction action, String entityType, LocalDate from,
                                                 LocalDate to, Integer page, Integer size) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw ApiException.validation("to", "Đến ngày phải sau hoặc bằng từ ngày");
            }
            if (ChronoUnit.DAYS.between(from, to) > MAX_RANGE_DAYS) {
                throw ApiException.validation("to", "Khoảng thời gian tối đa " + MAX_RANGE_DAYS + " ngày");
            }
        }
        String type = entityType == null || entityType.isBlank() ? null : entityType.trim().toUpperCase();
        return PageResponse.of(auditLogRepository.search(Paging.keyword(q), action, type,
                from == null ? null : from.atStartOfDay(),
                to == null ? null : to.plusDays(1).atStartOfDay(),
                Paging.of(page, size)), AdminMapper::audit);
    }

    private static String clientIp() {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs)) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        String forwarded = request.getHeader("X-Forwarded-For");
        String ip = forwarded != null && !forwarded.isBlank() ? forwarded.split(",")[0].trim() : request.getRemoteAddr();
        return ip != null && ip.length() > 45 ? ip.substring(0, 45) : ip;
    }
}
