package com.insurancecard.service.impl;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.common.TemporaryPassword;
import com.insurancecard.common.TextNormalizer;
import com.insurancecard.mapper.AdminMapper;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreateRequest;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreatedResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountUpdateRequest;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.AuditAction;
import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.service.AuditLogService;
import com.insurancecard.service.MailService;
import com.insurancecard.service.StaffAccountService;

@Service
public class StaffAccountServiceImpl implements StaffAccountService {

    private static final Set<Role> INTERNAL_ROLES = EnumSet.of(Role.STAFF, Role.ADMIN);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;
    private final AuditLogService auditLogService;
    private final CurrentUser currentUser;

    public StaffAccountServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder,
                                   MailService mailService, AuditLogService auditLogService,
                                   CurrentUser currentUser) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
        this.auditLogService = auditLogService;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StaffAccountResponse> search(String q, Role role, UserStatus status, Integer page,
                                                     Integer size) {
        if (role == Role.CUSTOMER) {
            throw ApiException.validation("role", "Chỉ lọc được vai trò STAFF hoặc ADMIN");
        }
        return PageResponse.of(userRepository.searchInternal(Paging.keyword(q), role, status, INTERNAL_ROLES,
                Paging.of(page, size)), AdminMapper::staff);
    }

    @Override
    @Transactional(readOnly = true)
    public StaffAccountResponse get(Long id) {
        return AdminMapper.staff(find(id));
    }

    @Override
    @Transactional
    public StaffAccountCreatedResponse create(StaffAccountCreateRequest r) {
        checkRole(r.role());
        String email = TextNormalizer.normalizeEmail(r.email());
        checkDuplicates(email, r.phone(), null);

        String tempPassword = TemporaryPassword.generate();
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(tempPassword));
        user.setFullName(TextNormalizer.normalizeName(r.fullName()));
        user.setPhone(r.phone());
        user.setRole(r.role());
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);

        mailService.send(email, "[InsuranceCard] Tài khoản nội bộ của bạn đã được tạo",
                "Xin chào " + user.getFullName() + ",\n\nQuản trị viên đã tạo tài khoản " + roleLabel(r.role())
                        + " cho bạn.\nEmail: " + email + "\nMật khẩu tạm thời: " + tempPassword
                        + "\n\nVui lòng đăng nhập và đổi mật khẩu ngay.");
        auditLogService.record(AuditAction.CREATE, "USER", user.getId(),
                "Tạo tài khoản " + roleLabel(r.role()) + " " + email);
        return new StaffAccountCreatedResponse(AdminMapper.staff(user), tempPassword);
    }

    @Override
    @Transactional
    public StaffAccountResponse update(Long id, StaffAccountUpdateRequest r) {
        User user = find(id);
        checkRole(r.role());
        if (r.status() == UserStatus.PENDING) {
            throw ApiException.validation("status", "Chỉ được chọn ACTIVE (hoạt động) hoặc LOCKED (khoá)");
        }
        boolean isSelf = user.getId().equals(currentUser.get().userId());
        if (isSelf && (r.status() != UserStatus.ACTIVE || r.role() != user.getRole())) {
            throw ApiException.business("CANNOT_CHANGE_SELF",
                    "Không được tự khoá hoặc tự đổi vai trò của chính mình");
        }
        boolean losesAdmin = user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE
                && (r.role() != Role.ADMIN || r.status() != UserStatus.ACTIVE);
        if (losesAdmin) {
            ensureNotLastAdmin();
        }
        checkDuplicates(user.getEmail(), r.phone(), user.getId());

        UserStatus oldStatus = user.getStatus();
        user.setFullName(TextNormalizer.normalizeName(r.fullName()));
        user.setPhone(r.phone());
        user.setRole(r.role());
        user.setStatus(r.status());
        if (oldStatus != UserStatus.ACTIVE && r.status() == UserStatus.ACTIVE) {
            user.setFailedLoginCount(0);
            user.setLockoutUntil(null);
        }
        if (oldStatus != r.status()) {
            auditLogService.record(AuditAction.STATUS_CHANGE, "USER", user.getId(),
                    (r.status() == UserStatus.LOCKED ? "Khoá" : "Mở khoá") + " tài khoản " + user.getEmail());
        } else {
            auditLogService.record(AuditAction.UPDATE, "USER", user.getId(), "Sửa tài khoản " + user.getEmail());
        }
        return AdminMapper.staff(user);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        User user = find(id);
        if (user.getId().equals(currentUser.get().userId())) {
            throw ApiException.business("CANNOT_CHANGE_SELF", "Không được tự xoá tài khoản của chính mình");
        }
        if (user.getRole() == Role.ADMIN && user.getStatus() == UserStatus.ACTIVE) {
            ensureNotLastAdmin();
        }
        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // Đã đăng nhập / tạo / xử lý hồ sơ -> bị khoá ngoại chặn (BR-24.4)
            throw ApiException.conflict("STAFF_HAS_DATA",
                    "Tài khoản đã phát sinh dữ liệu, không thể xoá. Hãy khoá tài khoản thay vì xoá.");
        }
        auditLogService.record(AuditAction.DELETE, "USER", id, "Xoá tài khoản " + user.getEmail());
    }

    private User find(Long id) {
        return userRepository.findById(id)
                .filter(u -> INTERNAL_ROLES.contains(u.getRole()))
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy tài khoản nhân viên"));
    }

    private static void checkRole(Role role) {
        if (!INTERNAL_ROLES.contains(role)) {
            throw ApiException.validation("role", "Vai trò phải là STAFF (nhân viên) hoặc ADMIN (quản trị viên)");
        }
    }

    private void ensureNotLastAdmin() {
        if (userRepository.countByRoleAndStatus(Role.ADMIN, UserStatus.ACTIVE) <= 1) {
            throw ApiException.business("LAST_ADMIN",
                    "Phải còn ít nhất 1 quản trị viên đang hoạt động");
        }
    }

    private void checkDuplicates(String email, String phone, Long userId) {
        Map<String, String> duplicates = new LinkedHashMap<>();
        if (userId == null && userRepository.existsByEmailIgnoreCase(email)) {
            duplicates.put("email", "Email đã được sử dụng");
        }
        boolean phoneTaken = userId == null ? userRepository.existsByPhone(phone)
                : userRepository.existsByPhoneAndIdNot(phone, userId);
        if (phoneTaken) {
            duplicates.put("phone", "Số điện thoại đã được sử dụng");
        }
        if (!duplicates.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "DUPLICATE", "Thông tin đã tồn tại trong hệ thống", duplicates);
        }
    }

    private static String roleLabel(Role role) {
        return role == Role.ADMIN ? "quản trị viên" : "nhân viên";
    }
}
