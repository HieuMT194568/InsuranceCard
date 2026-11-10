-- =====================================================================
--  V10 (Hiếu – M01/M02/M03): vai trò ADMIN, nhật ký hệ thống, thông báo,
--  danh mục gara liên kết.
--  Dải version của Hiếu: V10 – V19. Không sửa file này sau khi đã merge.
-- =====================================================================

-- M01: thêm vai trò ADMIN
ALTER TABLE users DROP CHECK ck_users_role;
ALTER TABLE users ADD CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'STAFF', 'ADMIN'));

-- M02: nhật ký thao tác quan trọng (chỉ thêm, không sửa / xoá)
CREATE TABLE audit_logs (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    user_id     BIGINT       NULL,
    action      VARCHAR(30)  NOT NULL,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NULL,
    description VARCHAR(500) NOT NULL,
    ip_address  VARCHAR(45)  NULL,
    created_at  DATETIME(6)  NOT NULL,
    CONSTRAINT pk_audit_logs PRIMARY KEY (id),
    CONSTRAINT fk_audit_logs_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_audit_logs_action CHECK (action IN ('LOGIN', 'LOGIN_FAILED', 'CREATE', 'UPDATE', 'DELETE',
                                                      'STATUS_CHANGE', 'PAYMENT'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_audit_logs_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX ix_audit_logs_created ON audit_logs (created_at);

-- M02: gara liên kết (claim_assessments của Hoàng trỏ khoá ngoại tới bảng này)
CREATE TABLE garages (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    garage_code VARCHAR(20)  NOT NULL,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255) NOT NULL,
    province    VARCHAR(50)  NOT NULL,
    phone       VARCHAR(10)  NOT NULL,
    email       VARCHAR(100) NULL,
    status      VARCHAR(20)  NOT NULL,
    note        VARCHAR(500) NULL,
    created_at  DATETIME(6)  NOT NULL,
    updated_at  DATETIME(6)  NOT NULL,
    CONSTRAINT pk_garages PRIMARY KEY (id),
    CONSTRAINT uq_garages_code UNIQUE (garage_code),
    CONSTRAINT ck_garages_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_garages_province_status ON garages (province, status);

-- M03: thông báo trong ứng dụng
CREATE TABLE notifications (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    user_id    BIGINT       NOT NULL,
    type       VARCHAR(30)  NOT NULL,
    title      VARCHAR(150) NOT NULL,
    content    VARCHAR(500) NOT NULL,
    link       VARCHAR(255) NULL,
    ref_key    VARCHAR(100) NULL,
    is_read    BOOLEAN      NOT NULL DEFAULT FALSE,
    read_at    DATETIME(6)  NULL,
    created_at DATETIME(6)  NOT NULL,
    CONSTRAINT pk_notifications PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_notifications_type CHECK (type IN ('CONTRACT_ACTIVATED', 'CONTRACT_EXPIRING', 'CONTRACT_CANCELLED',
                                                     'CERTIFICATE_ISSUED', 'PENALTY_ISSUED', 'ACCIDENT_RESOLVED',
                                                     'CLAIM_UPDATED', 'TICKET_REPLIED', 'SYSTEM'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE INDEX ix_notifications_user_read ON notifications (user_id, is_read, created_at);
CREATE INDEX ix_notifications_user_ref ON notifications (user_id, ref_key);
