// M03 (Hiếu) – UC-26 Thông báo của người đang đăng nhập
import client from './client';
import { clean, data } from './http';

export const notificationApi = {
  list: (params) => data(client.get('/notifications', { params: clean(params) })),
  unreadCount: () => data(client.get('/notifications/unread-count')),
  markRead: (id) => data(client.post(`/notifications/${id}/read`)),
  markAllRead: () => data(client.post('/notifications/read-all')),
};
