// M02 (Hiếu) – UC-24 Quản lý tài khoản nhân viên (chỉ ADMIN)
import client from './client';
import { clean, data } from './http';

export const staffAccountApi = {
  search: (params) => data(client.get('/staff-accounts', { params: clean(params) })),
  get: (id) => data(client.get(`/staff-accounts/${id}`)),
  create: (body) => data(client.post('/staff-accounts', body)),
  update: (id, body) => data(client.put(`/staff-accounts/${id}`, body)),
  remove: (id) => data(client.delete(`/staff-accounts/${id}`)),
};
