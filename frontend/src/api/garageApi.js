// M02 (Hiếu) – UC-43 Quản lý gara liên kết, UC-44 danh sách công khai
import client from './client';
import { clean, data } from './http';

export const garageApi = {
  search: (params) => data(client.get('/garages', { params: clean(params) })),
  get: (id) => data(client.get(`/garages/${id}`)),
  create: (body) => data(client.post('/garages', body)),
  update: (id, body) => data(client.put(`/garages/${id}`, body)),
  remove: (id) => data(client.delete(`/garages/${id}`)),
  listPublic: (params) => data(client.get('/public/garages', { params: clean(params) })),
};
