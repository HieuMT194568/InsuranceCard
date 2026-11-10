package com.insurancecard.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.CodeGenerator;
import com.insurancecard.common.ApiException;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.mapper.DtoMapper;
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
import com.insurancecard.repository.PaymentRepository;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.service.PaymentService;

/** Mọi giao dịch tiền (thu phí, hoàn phí, thu phạt, chi bồi thường) đều ghi vào bảng payments. */
@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final CurrentUser currentUser;

    public PaymentServiceImpl(PaymentRepository paymentRepository, CurrentUser currentUser) {
        this.paymentRepository = paymentRepository;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> search(String q, Long customerId, PaymentType type, PaymentStatus status,
                                                Integer page, Integer size) {
        return PageResponse.of(paymentRepository.search(Paging.keyword(q), currentUser.scopeCustomerId(customerId),
                type, status, Paging.of(page, size)), DtoMapper::payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse get(Long id) {
        Payment p = paymentRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy giao dịch"));
        currentUser.checkOwnership(p.getCustomer().getId());
        return DtoMapper.payment(p);
    }

    @Override
    public Payment createPending(Customer customer, Contract contract, PaymentType type, BigDecimal amount,
                                 String note) {
        Payment p = newPayment(customer, type, amount, note);
        p.setContract(contract);
        p.setStatus(PaymentStatus.PENDING);
        return paymentRepository.save(p);
    }

    @Override
    public Payment createPaid(Customer customer, Contract contract, Punishment punishment, Compensation compensation,
                              PaymentType type, BigDecimal amount, PaymentMethod method, String note,
                              User processedBy) {
        Payment p = newPayment(customer, type, amount, note);
        p.setContract(contract);
        p.setPunishment(punishment);
        p.setCompensation(compensation);
        p.setStatus(PaymentStatus.PAID);
        p.setMethod(method);
        p.setPaidAt(LocalDateTime.now());
        p.setProcessedBy(processedBy);
        return paymentRepository.save(p);
    }

    @Override
    public void markPaid(Payment p, PaymentMethod method, User processedBy) {
        p.setStatus(PaymentStatus.PAID);
        p.setMethod(method);
        p.setPaidAt(LocalDateTime.now());
        p.setProcessedBy(processedBy);
    }

    private Payment newPayment(Customer customer, PaymentType type, BigDecimal amount, String note) {
        Payment p = new Payment();
        p.setPaymentCode(CodeGenerator.next("PM", paymentRepository::existsByPaymentCode));
        p.setCustomer(customer);
        p.setType(type);
        p.setAmount(amount);
        p.setNote(note);
        return p;
    }
}
