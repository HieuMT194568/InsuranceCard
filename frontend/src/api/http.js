/**
 * Hàm dùng chung cho các file API của từng module (quy ước SRS: module mới tạo file api/<module>Api.js riêng,
 * không thêm vào services.js để tránh xung đột khi merge).
 */
export const data = (p) => p.then((r) => r.data);

/** Bỏ các tham số rỗng khỏi query string. */
export const clean = (params = {}) =>
  Object.fromEntries(Object.entries(params).filter(([, v]) => v !== '' && v !== null && v !== undefined));
