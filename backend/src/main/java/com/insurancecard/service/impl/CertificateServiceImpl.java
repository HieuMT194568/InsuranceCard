package com.insurancecard.service.impl;

import org.springframework.stereotype.Service;

import com.insurancecard.model.entity.Contract;
import com.insurancecard.service.CertificateService;

/**
 * Bản rỗng (stub) của M12 tạo ở Bước 0 – TODO(Khang): thay bằng logic cấp / thu hồi GCN thật.
 */
@Service
public class CertificateServiceImpl implements CertificateService {

    @Override
    public Long issueFor(Contract contract) {
        return null;
    }

    @Override
    public void revokeByContract(Long contractId, String reason) {
        // TODO(Khang): chuyển GCN sang REVOKED
    }

    @Override
    public Long findIdByContract(Long contractId) {
        return null;
    }
}
