package com.insurancecard.config.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.config.AppProperties;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;
import com.insurancecard.repository.UserRepository;

/**
 * M02 (Hiếu) – bảo đảm luôn có 1 tài khoản quản trị viên, kể cả với DB đã có dữ liệu từ trước.
 * Quy ước seeder: DataSeeder chạy @Order(1); mỗi thành viên tạo config/seed/&lt;Module&gt;Seeder với
 * @Order trong dải của mình (Hiếu 10-19, Quyên 20-29, Minh 30-39, Khang 40-49, Hoàng 50-59).
 */
@Component
@Order(10)
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    static final String ADMIN_EMAIL = "admin@insurancecard.vn";

    private final AppProperties props;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AdminSeeder(AppProperties props, UserRepository users, PasswordEncoder encoder) {
        this.props = props;
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!props.seed().enabled() || users.existsByRole(Role.ADMIN) || users.existsByEmailIgnoreCase(ADMIN_EMAIL)) {
            return;
        }
        String phone = "0901234500";
        if (users.existsByPhone(phone)) {
            phone = "0901234599";
        }
        User admin = new User();
        admin.setEmail(ADMIN_EMAIL);
        admin.setPasswordHash(encoder.encode("Admin@123"));
        admin.setFullName("Quản Trị Viên");
        admin.setPhone(phone);
        admin.setRole(Role.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        users.save(admin);
        log.info("Tạo tài khoản quản trị viên mặc định {}", ADMIN_EMAIL);
    }
}
