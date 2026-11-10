package com.insurancecard.service;

import java.math.BigDecimal;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.PaymentDtos.PaymentResponse;
import com.insurancecard.model.entity.Compensation;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.entity.Customer;
import com.insurancecard.model.entity.Payment;
import com.insurancecard.model.entity.Punishment;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.PaymentMethod;
import com.insurancecard.model.enums.PaymentStatus;
import com.insurancecard.model.enums.PaymentType;

/** Mọi giao dịch tiền (thu phí, hoàn phí, thu phạt, chi bồi thường) đều ghi vào bảng payments. */
public interface PaymentService {

    PageResponse<PaymentResponse> search(String q, Long customerId, PaymentType type, PaymentStatus status,
                                         Integer page, Integer size);

    PaymentResponse get(Long id);

    Payment createPending(Customer customer, Contract contract, PaymentType type, BigDecimal amount,
                          String note);

    Payment createPaid(Customer customer, Contract contract, Punishment punishment, Compensation compensation,
                       PaymentType type, BigDecimal amount, PaymentMethod method, String note,
                       User processedBy);

    void markPaid(Payment p, PaymentMethod method, User processedBy);
}
