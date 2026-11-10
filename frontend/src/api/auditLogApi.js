// M02 (Hiếu) – UC-25 Nhật ký hệ thống (chỉ ADMIN, chỉ đọc)
import client from './client';
import { clean, data } from './http';

export const auditLogApi = {
  search: (params) => data(client.get('/audit-logs', { params: clean(params) })),
};
