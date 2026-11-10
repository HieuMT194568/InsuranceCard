package com.insurancecard.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.mapper.NotificationMapper;
import com.insurancecard.model.dto.NotificationDtos.NotificationResponse;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.entity.Notification;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.NotificationType;
import com.insurancecard.repository.NotificationRepository;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.service.ContractService;
import com.insurancecard.service.MailService;
import com.insurancecard.service.NotificationService;
import com.insurancecard.service.SettingService;

@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);
    private static final int MAX_TITLE = 150;
    private static final int MAX_CONTENT = 500;
    private static final int MAX_LINK = 255;
    private static final int MAX_REF_KEY = 100;

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final ContractService contractService;
    private final SettingService settingService;
    private final MailService mailService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UserRepository userRepository,
                                   CurrentUser currentUser, ContractService contractService,
                                   SettingService settingService, MailService mailService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
        this.contractService = contractService;
        this.settingService = settingService;
        this.mailService = mailService;
    }

    @Override
    @Transactional
    public void notify(Long userId, NotificationType type, String title, String content, String link, String refKey) {
        try {
            if (userId == null || type == null || !userRepository.existsById(userId)) {
                return;
            }
            String key = cut(refKey, MAX_REF_KEY);
            if (key != null && notificationRepository.existsByUserIdAndRefKey(userId, key)) {
                return;
            }
            Notification n = new Notification();
            n.setUser(userRepository.getReferenceById(userId));
            n.setType(type);
            n.setTitle(orDefault(cut(title, MAX_TITLE), "Thông báo"));
            n.setContent(orDefault(cut(content, MAX_CONTENT), ""));
            n.setLink(cut(link, MAX_LINK));
            n.setRefKey(key);
            notificationRepository.save(n);
        } catch (RuntimeException e) {
            // BR-27.1: thông báo lỗi không được làm hỏng nghiệp vụ gọi nó
            log.warn("Không tạo được thông báo {} cho user {}: {}", type, userId, e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> listMine(boolean unreadOnly, Integer page, Integer size) {
        return PageResponse.of(notificationRepository.findForUser(currentUser.get().userId(), unreadOnly,
                Paging.of(page, size)), NotificationMapper::notification);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnreadMine() {
        return notificationRepository.countByUserIdAndReadFalse(currentUser.get().userId());
    }

    @Override
    @Transactional
    public NotificationResponse markRead(Long id) {
        // Thông báo của người khác -> 404 (không lộ sự tồn tại)
        Notification n = notificationRepository.findByIdAndUserId(id, currentUser.get().userId())
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy thông báo"));
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(LocalDateTime.now());
        }
        return NotificationMapper.notification(n);
    }

    @Override
    @Transactional
    public int markAllRead() {
        return notificationRepository.markAllRead(currentUser.get().userId(), LocalDateTime.now());
    }

    @Override
    @Transactional
    public int remindExpiringContracts() {
        LocalDate today = LocalDate.now();
        int windowDays = settingService.getInt(SettingService.RENEWAL_WINDOW_DAYS);
        List<Contract> contracts = contractService.findExpiringWithoutRenewal(today, today.plusDays(windowDays));
        int created = 0;
        for (Contract c : contracts) {
            User owner = c.getCustomer().getUser();
            String refKey = "CONTRACT_EXPIRING:" + c.getId();
            if (notificationRepository.existsByUserIdAndRefKey(owner.getId(), refKey)) {
                continue;
            }
            long daysLeft = ChronoUnit.DAYS.between(today, c.getEndDate());
            String title = "Hợp đồng " + c.getContractNumber() + " sắp hết hạn";
            String content = "Hợp đồng bảo hiểm " + c.getContractNumber() + " hết hạn vào " + c.getEndDate()
                    + " (còn " + daysLeft + " ngày). Hãy gia hạn để không bị gián đoạn quyền lợi bảo hiểm.";
            notify(owner.getId(), NotificationType.CONTRACT_EXPIRING, title, content,
                    "/customer/contracts/" + c.getId(), refKey);
            try {
                mailService.send(owner.getEmail(), "[InsuranceCard] " + title,
                        "Xin chào " + owner.getFullName() + ",\n\n" + content + "\n\nTrân trọng,\nInsuranceCard");
            } catch (RuntimeException e) {
                // Gửi email lỗi vẫn giữ thông báo trong ứng dụng (luồng 5a của UC-28)
                log.warn("Không gửi được email nhắc hạn HĐ {}: {}", c.getContractNumber(), e.getMessage());
            }
            created++;
        }
        return created;
    }

    private static String cut(String s, int max) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.length() > max ? t.substring(0, max) : t;
    }

    private static String orDefault(String s, String fallback) {
        return s == null ? fallback : s;
    }
}
