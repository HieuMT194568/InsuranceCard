package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.ContractDtos.PayRequest;
import com.insurancecard.model.dto.PunishmentDtos.PunishmentRequest;
import com.insurancecard.model.dto.PunishmentDtos.PunishmentResponse;
import com.insurancecard.model.dto.PunishmentDtos.WaiveRequest;
import com.insurancecard.model.enums.PunishmentStatus;

public interface PunishmentService {

    PageResponse<PunishmentResponse> search(String q, Long customerId, PunishmentStatus status,
                                            Integer page, Integer size);

    PunishmentResponse get(Long id);

    PunishmentResponse create(PunishmentRequest r);

    PunishmentResponse update(Long id, PunishmentRequest r);

    /** Chỉ xoá được biên bản chưa nộp (lập nhầm). */
    void delete(Long id);

    PunishmentResponse pay(Long id, PayRequest r);

    PunishmentResponse waive(Long id, WaiveRequest r);
}
