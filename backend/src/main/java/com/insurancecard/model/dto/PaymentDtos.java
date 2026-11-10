package com.insurancecard.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.insurancecard.model.enums.PaymentMethod;
import com.insurancecard.model.enums.PaymentStatus;
import com.insurancecard.model.enums.PaymentType;

public final class PaymentDtos {

    private PaymentDtos() {}

    public record PaymentResponse(
            Long id,
            String paymentCode,
            PaymentType type,
            BigDecimal amount,
            PaymentMethod method,
            PaymentStatus status,
            LocalDateTime paidAt,
            String note,
            Long customerId,
            String customerName,
            Long contractId,
            String contractNumber,
            Long punishmentId,
            String punishmentCode,
            Long compensationId,
            String claimCode,
            LocalDateTime createdAt) {}
}
