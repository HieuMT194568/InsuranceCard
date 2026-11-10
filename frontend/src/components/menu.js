/**
 * Menu bên trái theo vai trò (chủ file: Hiếu).
 * QUY ƯỚC: mỗi thành viên chỉ thêm / sửa dòng nằm dưới comment tên mình, không đụng dòng của người khác.
 * Mục menu: { to, icon, label, end?, roles? } – `to` là đường dẫn con của /customer hoặc /staff;
 * `roles` (tuỳ chọn) giới hạn vai trò được thấy, ví dụ ['ADMIN']. Nhóm không còn mục nào sẽ tự ẩn.
 * Chỉ thêm mục khi trang đã có route trong App.jsx (tránh link tới trang 404).
 */

export const CUSTOMER_MENU = [
  {
    heading: 'Tổng quan',
    items: [
      // --- KHANG
      { to: 'dashboard', icon: 'bi-speedometer2', label: 'Bảng điều khiển' },
      // --- QUYÊN
      { to: 'profile', icon: 'bi-person-circle', label: 'Hồ sơ cá nhân' },
      // --- HIẾU
      { to: 'notifications', icon: 'bi-bell', label: 'Thông báo' },
    ],
  },
  {
    heading: 'Bảo hiểm',
    items: [
      // --- QUYÊN
      { to: 'vehicles', icon: 'bi-scooter', label: 'Xe của tôi' },
      // --- MINH
      { to: 'contracts/new', icon: 'bi-cart-plus', label: 'Mua bảo hiểm' },
      { to: 'contracts', icon: 'bi-file-earmark-text', label: 'Hợp đồng', end: true },
      // --- KHANG  (vd: { to: 'certificates', icon: 'bi-patch-check', label: 'Giấy chứng nhận' })
    ],
  },
  {
    heading: 'Lịch sử',
    items: [
      // --- KHANG
      { to: 'payments', icon: 'bi-credit-card', label: 'Thanh toán' },
      // --- HOÀNG
      { to: 'accidents', icon: 'bi-exclamation-octagon', label: 'Tai nạn' },
      { to: 'compensations', icon: 'bi-cash-coin', label: 'Bồi thường' },
      // --- KHANG
      { to: 'punishments', icon: 'bi-shield-exclamation', label: 'Vi phạm / phạt' },
    ],
  },
  {
    heading: 'Hỗ trợ',
    items: [
      // --- QUYÊN  (vd: { to: 'support', icon: 'bi-chat-dots', label: 'Yêu cầu hỗ trợ' })
    ],
  },
];

export const STAFF_MENU = [
  {
    heading: 'Tổng quan',
    items: [
      // --- KHANG
      { to: 'dashboard', icon: 'bi-speedometer2', label: 'Bảng điều khiển' },
      // --- HIẾU
      { to: 'notifications', icon: 'bi-bell', label: 'Thông báo' },
    ],
  },
  {
    heading: 'Khách hàng',
    items: [
      // --- QUYÊN
      { to: 'customers', icon: 'bi-people', label: 'Khách hàng' },
      { to: 'vehicles', icon: 'bi-scooter', label: 'Xe' },
      // --- QUYÊN  (vd: { to: 'support', icon: 'bi-chat-dots', label: 'Yêu cầu hỗ trợ' })
    ],
  },
  {
    heading: 'Hợp đồng & sản phẩm',
    items: [
      // --- MINH
      { to: 'contracts', icon: 'bi-file-earmark-text', label: 'Hợp đồng' },
      { to: 'products', icon: 'bi-box-seam', label: 'Sản phẩm bảo hiểm' },
      // --- MINH  (vd: { to: 'promotions', icon: 'bi-ticket-perforated', label: 'Khuyến mãi' })
    ],
  },
  {
    heading: 'Thanh toán & vi phạm',
    items: [
      // --- KHANG
      { to: 'payments', icon: 'bi-credit-card', label: 'Giao dịch' },
      { to: 'punishments', icon: 'bi-shield-exclamation', label: 'Xử lý vi phạm' },
      // --- KHANG  (vd: reports, certificates, payment-channels)
    ],
  },
  {
    heading: 'Bồi thường',
    items: [
      // --- HOÀNG
      { to: 'accidents', icon: 'bi-exclamation-octagon', label: 'Xử lý tai nạn' },
      { to: 'compensations', icon: 'bi-cash-coin', label: 'Xử lý bồi thường' },
      // --- HOÀNG  (vd: { to: 'assessments', icon: 'bi-clipboard-check', label: 'Biên bản giám định' })
    ],
  },
  {
    heading: 'Danh mục',
    items: [
      // --- HIẾU
      { to: 'garages', icon: 'bi-tools', label: 'Gara liên kết' },
    ],
  },
  {
    heading: 'Quản trị hệ thống',
    items: [
      // --- HIẾU (chỉ ADMIN)
      { to: 'staff-accounts', icon: 'bi-person-badge', label: 'Tài khoản nhân viên', roles: ['ADMIN'] },
      { to: 'audit-logs', icon: 'bi-journal-text', label: 'Nhật ký hệ thống', roles: ['ADMIN'] },
      { to: 'settings', icon: 'bi-gear', label: 'Cấu hình', roles: ['ADMIN'] },
    ],
  },
  {
    heading: 'Tài khoản',
    items: [
      // --- QUYÊN
      { to: 'profile', icon: 'bi-person-circle', label: 'Hồ sơ cá nhân' },
    ],
  },
];

/** Menu đã lọc theo vai trò; bỏ nhóm rỗng. */
export function menuFor(role) {
  const groups = role === 'CUSTOMER' ? CUSTOMER_MENU : STAFF_MENU;
  return groups
    .map((g) => ({ ...g, items: g.items.filter((m) => !m.roles || m.roles.includes(role)) }))
    .filter((g) => g.items.length > 0);
}
