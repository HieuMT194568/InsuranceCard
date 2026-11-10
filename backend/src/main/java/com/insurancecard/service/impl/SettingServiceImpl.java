package com.insurancecard.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.model.dto.SettingDtos.SettingResponse;
import com.insurancecard.model.entity.SystemSetting;
import com.insurancecard.repository.SystemSettingRepository;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.model.enums.AuditAction;
import com.insurancecard.service.AuditLogService;
import com.insurancecard.service.SettingService;

@Service
public class SettingServiceImpl implements SettingService {

    private final SystemSettingRepository repository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final AuditLogService auditLogService;

    public SettingServiceImpl(SystemSettingRepository repository, UserRepository userRepository, CurrentUser currentUser,
                              AuditLogService auditLogService) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.auditLogService = auditLogService;
    }

    @Override
    @Transactional(readOnly = true)
    public int getInt(String key) {
        return repository.findById(key)
                .map(SystemSetting::getValue)
                .orElseThrow(() -> new IllegalStateException("Thiếu cấu hình hệ thống: " + key));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SettingResponse> list() {
        return repository.findAllByOrderByKeyAsc().stream().map(SettingServiceImpl::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SettingResponse get(String key) {
        return toResponse(find(key));
    }

    @Override
    @Transactional
    public SettingResponse update(String key, Integer value) {
        SystemSetting setting = find(key);
        if (value == null) {
            throw ApiException.validation("value", "Vui lòng nhập giá trị");
        }
        if (value < setting.getMinValue() || value > setting.getMaxValue()) {
            throw ApiException.validation("value",
                    "Giá trị phải từ " + setting.getMinValue() + " đến " + setting.getMaxValue());
        }
        int oldValue = setting.getValue();
        setting.setValue(value);
        setting.setUpdatedAt(LocalDateTime.now());
        setting.setUpdatedBy(userRepository.getReferenceById(currentUser.get().userId()));
        auditLogService.record(AuditAction.UPDATE, "SETTING", null,
                "Đổi cấu hình " + key + ": " + oldValue + " -> " + value);
        return toResponse(setting);
    }

    private SystemSetting find(String key) {
        return repository.findById(key)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy cấu hình " + key));
    }

    private static SettingResponse toResponse(SystemSetting s) {
        return new SettingResponse(s.getKey(), s.getValue(), s.getMinValue(), s.getMaxValue(), s.getUnit(),
                s.getDescription(), s.getUpdatedAt());
    }
}
