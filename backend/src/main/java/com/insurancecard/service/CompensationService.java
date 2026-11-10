package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.CompensationDtos.ApproveRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimUpdateRequest;
import com.insurancecard.model.dto.CompensationDtos.CompensationResponse;
import com.insurancecard.model.dto.CompensationDtos.PayoutRequest;
import com.insurancecard.model.dto.CompensationDtos.RejectRequest;
import com.insurancecard.model.entity.Compensation;
import com.insurancecard.model.enums.ClaimStatus;

public interface CompensationService {

    PageResponse<CompensationResponse> search(String q, Long customerId, ClaimStatus status,
                                              Integer page, Integer size);

    CompensationResponse get(Long id);

    CompensationResponse create(ClaimRequest r);

    /** Sửa số tiền / nội dung khi yêu cầu còn chờ duyệt. */
    CompensationResponse update(Long id, ClaimUpdateRequest r);

    /** Rút (xoá) yêu cầu còn chờ duyệt; tai nạn vẫn được giữ lại. */
    void delete(Long id);

    CompensationResponse approve(Long id, ApproveRequest r);

    CompensationResponse reject(Long id, RejectRequest r);

    /** Chi trả tiền bồi thường đã duyệt -> ghi nhận giao dịch COMPENSATION. */
    CompensationResponse payout(Long id, PayoutRequest r);

    Compensation find(Long id);
}
