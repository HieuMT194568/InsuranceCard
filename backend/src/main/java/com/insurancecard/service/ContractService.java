package com.insurancecard.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.ContractDtos.CancelRequest;
import com.insurancecard.model.dto.ContractDtos.ContractCreateRequest;
import com.insurancecard.model.dto.ContractDtos.ContractDetailResponse;
import com.insurancecard.model.dto.ContractDtos.ContractResponse;
import com.insurancecard.model.dto.ContractDtos.ContractUpdateRequest;
import com.insurancecard.model.dto.ContractDtos.PayRequest;
import com.insurancecard.model.dto.ContractDtos.RenewRequest;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.enums.ContractStatus;

public interface ContractService {

    PageResponse<ContractResponse> search(String q, Long customerId, Long vehicleId, ContractStatus status,
                                          Integer page, Integer size);

    ContractDetailResponse getDetail(Long id);

    ContractDetailResponse create(ContractCreateRequest r);

    ContractDetailResponse update(Long id, ContractUpdateRequest r);

    ContractDetailResponse pay(Long id, PayRequest r);

    ContractDetailResponse renew(Long id, RenewRequest r);

    ContractDetailResponse cancel(Long id, CancelRequest r);

    /**
     * Hoàn phí khi huỷ hợp đồng đang hiệu lực = phí × (số ngày còn lại / tổng số ngày) × CANCEL_REFUND_PERCENT%,
     * làm tròn xuống bội số 1.000 VND. Không hoàn nếu hợp đồng đã phát sinh bồi thường.
     */
    BigDecimal computeRefund(Contract c);

    /** Hết hạn hợp đồng quá endDate; huỷ hợp đồng chờ thanh toán đã qua ngày bắt đầu. */
    int refreshStatuses();

    Contract find(Long id);

    Contract findOwned(Long id);

    /**
     * Cho M03 (nhắc hợp đồng sắp hết hạn – UC-28): HĐ ACTIVE có endDate trong [from, to]
     * và chưa có bản gia hạn (chưa huỷ). Thêm ở Bước 0; chủ module M08 là Minh.
     */
    List<Contract> findExpiringWithoutRenewal(LocalDate from, LocalDate to);
}
