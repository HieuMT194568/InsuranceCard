// M02 (Hiếu) – schema form gara liên kết, khớp GarageDtos.java + ValidationRules.GARAGE_CODE
import { z } from 'zod';
import { MSG, RX, enumField, f, optionalText, required, text } from '../validation';

export const GARAGE_CODE_RX = /^[A-Z0-9_-]{3,20}$/;
export const GARAGE_CODE_MSG = "Mã gara gồm 3-20 ký tự: chữ in hoa, số, '-' hoặc '_'";

export const garageSchema = z.object({
  garageCode: required('Vui lòng nhập mã gara')
    .transform((v) => v.toUpperCase())
    .pipe(z.string().regex(GARAGE_CODE_RX, GARAGE_CODE_MSG)),
  name: text('Tên gara', 2, 150),
  address: text('Địa chỉ', 5, 255),
  province: text('Tỉnh / thành phố', 2, 50),
  phone: f.phone,
  email: z.string().trim().max(100, 'Email tối đa 100 ký tự')
    .refine((v) => !v || RX.email.test(v), MSG.email)
    .transform((v) => (v ? v : null)),
  status: enumField(['ACTIVE', 'INACTIVE'], 'Vui lòng chọn trạng thái'),
  note: optionalText(500, 'Ghi chú tối đa 500 ký tự'),
});
