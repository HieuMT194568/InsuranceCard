package com.insurancecard.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.insurancecard.service.NotificationService;

/** M03 (Hiếu) – UC-28: 08:00 hằng ngày và khi khởi động, nhắc các hợp đồng sắp hết hạn. */
@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;

    public NotificationScheduler(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        run();
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Ho_Chi_Minh")
    public void run() {
        int created = notificationService.remindExpiringContracts();
        if (created > 0) {
            log.info("Đã nhắc {} hợp đồng sắp hết hạn", created);
        }
    }
}
