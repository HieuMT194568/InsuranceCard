// M02 (Hiếu) – schema form tài khoản nhân viên, khớp StaffAccountDtos.java
import { z } from 'zod';
import { enumField, f } from '../validation';

const role = enumField(['STAFF', 'ADMIN'], 'Vui lòng chọn vai trò');

export const staffAccountCreateSchema = z.object({
  fullName: f.fullName,
  email: f.email,
  phone: f.phone,
  role,
});

export const staffAccountUpdateSchema = z.object({
  fullName: f.fullName,
  phone: f.phone,
  role,
  status: enumField(['ACTIVE', 'LOCKED'], 'Vui lòng chọn trạng thái'),
});
