package com.insurancecard.service;

/**
 * Gửi email qua SMTP khi app.mail.enabled=true. Ở chế độ demo (mặc định) chỉ ghi log
 * nội dung email; link xác thực được trả về cho frontend qua trường devLink.
 */
public interface MailService {

    boolean isDemoMode();

    void send(String to, String subject, String body);
}
