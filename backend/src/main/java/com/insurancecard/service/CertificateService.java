package com.insurancecard.service;

import com.insurancecard.model.entity.Contract;

/**
 * M12 – Giấy chứng nhận bảo hiểm điện tử (chủ: Khang).
 * Bước 0: chỉ có chữ ký hàm + bản rỗng để M08 (Minh) gọi được ngay. Khang thay bằng logic thật
 * (bảng insurance_certificates, migration V40–V49).
 */
public interface CertificateService {

    /** Cấp GCN khi HĐ chuyển ACTIVE; gọi lại nhiều lần vẫn chỉ có 1 GCN. Trả về id GCN. */
    Long issueFor(Contract contract);

    /** Thu hồi GCN khi HĐ ACTIVE bị huỷ; HĐ chưa có GCN thì bỏ qua. */
    void revokeByContract(Long contractId, String reason);

    /** Id GCN của HĐ (null nếu chưa có) – để chi tiết HĐ hiện nút "Xem giấy chứng nhận". */
    Long findIdByContract(Long contractId);
}
