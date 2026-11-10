/**
 * Nhãn trạng thái cho <StatusBadge type="..." value="..." /> của các module MỚI (SRS v2).
 * Quy ước: mỗi thành viên chỉ thêm vào khối của mình để tránh xung đột khi merge.
 * Định dạng: TÊN_KIỂU: { GIÁ_TRỊ_ENUM: ['Nhãn tiếng Việt', 'màu bootstrap'] }.
 * Nhãn của các module có sẵn (v1) vẫn nằm ở utils/format.js – LABELS gộp cả hai.
 */

// ============================================================ HIẾU (M01, M02, M03)
const HIEU = {
  role: {
    CUSTOMER: ['Khách hàng', 'secondary'],
    STAFF: ['Nhân viên', 'primary'],
    ADMIN: ['Quản trị viên', 'dark'],
  },
  garage: {
    ACTIVE: ['Đang hợp tác', 'success'],
    INACTIVE: ['Ngừng hợp tác', 'secondary'],
  },
  auditAction: {
    LOGIN: ['Đăng nhập', 'success'],
    LOGIN_FAILED: ['Đăng nhập sai', 'danger'],
    CREATE: ['Thêm mới', 'primary'],
    UPDATE: ['Cập nhật', 'info'],
    DELETE: ['Xoá', 'danger'],
    STATUS_CHANGE: ['Đổi trạng thái', 'warning'],
    PAYMENT: ['Thanh toán', 'success'],
  },
};

// ============================================================ QUYÊN (M04, M05, M06)
const QUYEN = {
  // ticket: { OPEN: ['Chờ xử lý', 'warning'], ANSWERED: ['Đã phản hồi', 'info'], CLOSED: ['Đã đóng', 'secondary'] },
};

// ============================================================ MINH (M07, M08, M09)
const MINH = {
  // promotion: { ACTIVE: ['Đang áp dụng', 'success'], INACTIVE: ['Tạm dừng', 'secondary'] },
};

// ============================================================ KHANG (M10, M11, M12)
const KHANG = {
  // certificate: { VALID: ['Còn hiệu lực', 'success'], REVOKED: ['Đã thu hồi', 'danger'] },
};

// ============================================================ HOÀNG (M13, M14, M15)
const HOANG = {
  // assessment: { DRAFT: ['Nháp', 'secondary'], COMPLETED: ['Hoàn tất', 'success'] },
};

export const STATUS_LABELS = { ...HIEU, ...QUYEN, ...MINH, ...KHANG, ...HOANG };

/** Nhãn + biểu tượng cho từng loại thông báo (M03). */
export const NOTIFICATION_TYPES = {
  CONTRACT_ACTIVATED: ['Hợp đồng có hiệu lực', 'bi-file-earmark-check', 'success'],
  CONTRACT_EXPIRING: ['Hợp đồng sắp hết hạn', 'bi-hourglass-split', 'warning'],
  CONTRACT_CANCELLED: ['Hợp đồng bị huỷ', 'bi-file-earmark-x', 'danger'],
  CERTIFICATE_ISSUED: ['Giấy chứng nhận', 'bi-patch-check', 'success'],
  PENALTY_ISSUED: ['Biên bản phạt', 'bi-shield-exclamation', 'danger'],
  ACCIDENT_RESOLVED: ['Tai nạn', 'bi-exclamation-octagon', 'info'],
  CLAIM_UPDATED: ['Bồi thường', 'bi-cash-coin', 'primary'],
  TICKET_REPLIED: ['Hỗ trợ khách hàng', 'bi-chat-dots', 'info'],
  SYSTEM: ['Hệ thống', 'bi-info-circle', 'secondary'],
};
