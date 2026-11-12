import client from './client';
import { data } from './http';

export const chatbotApi = {
  chat: (message) => data(client.post('/public/chat', { message })),
};

export default chatbotApi;
