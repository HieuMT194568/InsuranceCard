package com.insurancecard.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.CodeGenerator;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.mapper.DtoMapper;
import com.insurancecard.model.dto.CompensationDtos.ApproveRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimUpdateRequest;
import com.insurancecard.model.dto.CompensationDtos.CompensationResponse;
import com.insurancecard.model.dto.CompensationDtos.PayoutRequest;
import com.insurancecard.model.dto.CompensationDtos.RejectRequest;
import com.insurancecard.model.entity.Accident;
import com.insurancecard.model.entity.Compensation;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.AccidentStatus;
import com.insurancecard.model.enums.ClaimStatus;
import com.insurancecard.model.enums.ContractStatus;
import com.insurancecard.model.enums.PaymentMethod;
import com.insurancecard.model.enums.PaymentType;
import com.insurancecard.repository.CompensationRepository;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.AppUserPrincipal;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.service.AccidentService;
import com.insurancecard.service.CompensationService;
import com.insurancecard.service.PaymentService;
import com.insurancecard.service.SettingService;

@Service
public class CompensationServiceImpl implements CompensationService {

    /** Một tai nạn chỉ có tối đa 1 yêu cầu đang xử lý/đã duyệt/đã chi (bị từ chối thì được yêu cầu lại). */
    private static final EnumSet<ClaimStatus> BLOCKING = EnumSet.of(ClaimStatus.PENDING, ClaimStatus.APPROVED,
            ClaimStatus.PAID);
    private static final EnumSet<ClaimStatus> PAYOUT = EnumSet.of(ClaimStatus.APPROVED, ClaimStatus.PAID);

    private final CompensationRepository compensationRepository;
    private final UserRepository userRepository;
    private final AccidentService accidentService;
    private final PaymentService paymentService;
    private final SettingService settingService;
    private final CurrentUser currentUser;

    public CompensationServiceImpl(CompensationRepository compensationRepository, UserRepository userRepository,
                               AccidentService accidentService, PaymentService paymentService,
                               SettingService settingService, CurrentUser currentUser) {
        this.compensationRepository = compensationRepository;
        this.userRepository = userRepository;
        this.accidentService = accidentService;
        this.paymentService = paymentService;
        this.settingService = settingService;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<CompensationResponse> search(String q, Long customerId, ClaimStatus status,
                                                     Integer page, Integer size) {
        return PageResponse.of(compensationRepository.search(Paging.keyword(q),
                currentUser.scopeCustomerId(customerId), status, Paging.of(page, size)), DtoMapper::compensation);
    }

    @Override
    @Transactional(readOnly = true)
    public CompensationResponse get(Long id) {
        return DtoMapper.compensation(findOwned(id));
    }

    // ------------------------------------------------------------------ Khách hàng / nhân viên tạo yêu cầu

    @Override
    @Transactional
    public CompensationResponse create(ClaimRequest r) {
        AppUserPrincipal me = currentUser.get();
        if ((r.accidentId() == null) == (r.accident() == null)) {
            throw ApiException.validation("accidentId",
                    "Vui lòng chọn tai nạn đã khai báo hoặc khai báo tai nạn mới (chọn một trong hai)");
        }
        Accident accident;
        if (r.accident() != null) {
            accident = accidentService.createEntity(r.accident(), "accident.contractId", "accident.accidentTime");
        } else {
            accident = accidentService.findOwned(r.accidentId());
            if (accident.getStatus() == AccidentStatus.REJECTED) {
                throw ApiException.businessField("ACCIDENT_REJECTED", "accidentId",
                        "Tai nạn đã bị bác bỏ, không thể yêu cầu bồi thường");
            }
            if (compensationRepository.existsByAccidentIdAndStatusIn(accident.getId(), BLOCKING)) {
                throw ApiException.conflict("CLAIM_EXISTS",
                        "Tai nạn này đã có yêu cầu bồi thường đang xử lý hoặc đã được chi trả");
            }
        }
        Contract contract = accident.getContract();
        if (contract.getStatus() != ContractStatus.ACTIVE && contract.getStatus() != ContractStatus.EXPIRED) {
            throw ApiException.business("CONTRACT_NOT_EFFECTIVE", "Hợp đồng không còn hiệu lực để yêu cầu bồi thường");
        }
        int deadline = settingService.getInt(SettingService.CLAIM_DEADLINE_DAYS);
        if (accident.getAccidentTime().toLocalDate().plusDays(deadline).isBefore(LocalDate.now())) {
            throw ApiException.business("CLAIM_DEADLINE_PASSED",
                    "Đã quá thời hạn yêu cầu bồi thường " + deadline + " ngày kể từ ngày xảy ra tai nạn");
        }
        checkRequestedAmount(r.requestedAmount(), accident, contract);

        Compensation cp = new Compensation();
        cp.setClaimCode(CodeGenerator.next("BT", compensationRepository::existsByClaimCode));
        cp.setAccident(accident);
        cp.setContract(contract);
        cp.setRequestedAmount(r.requestedAmount());
        cp.setDescription(r.description());
        cp.setStatus(ClaimStatus.PENDING);
        cp.setRequestedBy(userRepository.getReferenceById(me.userId()));
        compensationRepository.save(cp);
        return DtoMapper.compensation(cp);
    }

    @Override
    @Transactional
    public CompensationResponse update(Long id, ClaimUpdateRequest r) {
        Compensation cp = findOwned(id);
        requirePending(cp);
        checkRequestedAmount(r.requestedAmount(), cp.getAccident(), cp.getContract());
        cp.setRequestedAmount(r.requestedAmount());
        cp.setDescription(r.description());
        return DtoMapper.compensation(cp);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Compensation cp = findOwned(id);
        requirePending(cp);
        compensationRepository.delete(cp);
    }

    // ------------------------------------------------------------------ Nhân viên xử lý

    @Override
    @Transactional
    public CompensationResponse approve(Long id, ApproveRequest r) {
        Compensation cp = find(id);
        requirePending(cp);
        if (cp.getAccident().getStatus() != AccidentStatus.VERIFIED) {
            throw ApiException.business("ACCIDENT_NOT_VERIFIED",
                    "Cần xác minh tai nạn " + cp.getAccident().getAccidentCode() + " trước khi duyệt bồi thường");
        }
        if (r.approvedAmount().compareTo(cp.getRequestedAmount()) > 0) {
            throw ApiException.validation("approvedAmount",
                    "Số tiền duyệt không được vượt quá số tiền yêu cầu (" + vnd(cp.getRequestedAmount()) + ")");
        }
        BigDecimal remaining = remainingCoverage(cp.getContract(), cp.getId());
        if (r.approvedAmount().compareTo(remaining) > 0) {
            throw ApiException.businessField("EXCEEDS_COVERAGE", "approvedAmount",
                    "Số tiền duyệt vượt quá hạn mức bồi thường còn lại của hợp đồng (" + vnd(remaining) + ")");
        }
        cp.setStatus(ClaimStatus.APPROVED);
        cp.setApprovedAmount(r.approvedAmount());
        cp.setResolutionNote(r.note());
        markResolved(cp);
        return DtoMapper.compensation(cp);
    }

    @Override
    @Transactional
    public CompensationResponse reject(Long id, RejectRequest r) {
        Compensation cp = find(id);
        requirePending(cp);
        cp.setStatus(ClaimStatus.REJECTED);
        cp.setResolutionNote(r.note());
        markResolved(cp);
        return DtoMapper.compensation(cp);
    }

    @Override
    @Transactional
    public CompensationResponse payout(Long id, PayoutRequest r) {
        Compensation cp = find(id);
        if (cp.getStatus() != ClaimStatus.APPROVED) {
            throw ApiException.business("CLAIM_NOT_APPROVED", "Chỉ chi trả được yêu cầu đã được duyệt");
        }
        if (r.method() != PaymentMethod.CASH && r.method() != PaymentMethod.BANK_TRANSFER) {
            throw ApiException.validation("method", "Chi trả bồi thường chỉ qua tiền mặt hoặc chuyển khoản");
        }
        User staff = userRepository.getReferenceById(currentUser.get().userId());
        cp.setStatus(ClaimStatus.PAID);
        cp.setPaidAt(LocalDateTime.now());
        paymentService.createPaid(cp.getContract().getCustomer(), cp.getContract(), null, cp,
                PaymentType.COMPENSATION, cp.getApprovedAmount(), r.method(),
                "Chi trả bồi thường " + cp.getClaimCode(), staff);
        return DtoMapper.compensation(cp);
    }

    // ------------------------------------------------------------------ Tiện ích

    private void requirePending(Compensation cp) {
        if (cp.getStatus() != ClaimStatus.PENDING) {
            throw ApiException.business("CLAIM_RESOLVED", "Yêu cầu bồi thường đã được xử lý trước đó");
        }
    }

    /** Số tiền yêu cầu không vượt thiệt hại ước tính và hạn mức còn lại (hạn mức − tổng đã duyệt/đã chi). */
    private void checkRequestedAmount(BigDecimal amount, Accident accident, Contract contract) {
        if (amount.compareTo(accident.getEstimatedDamage()) > 0) {
            throw ApiException.businessField("EXCEEDS_DAMAGE", "requestedAmount",
                    "Số tiền yêu cầu không được vượt quá thiệt hại ước tính (" + vnd(accident.getEstimatedDamage()) + ")");
        }
        BigDecimal remaining = remainingCoverage(contract, null);
        if (amount.compareTo(remaining) > 0) {
            throw ApiException.businessField("EXCEEDS_COVERAGE", "requestedAmount",
                    "Số tiền yêu cầu vượt quá hạn mức bồi thường còn lại của hợp đồng (" + vnd(remaining) + ")");
        }
    }

    private void markResolved(Compensation cp) {
        cp.setResolvedAt(LocalDateTime.now());
        cp.setResolvedBy(userRepository.getReferenceById(currentUser.get().userId()));
    }

    private BigDecimal remainingCoverage(Contract c, Long excludeClaimId) {
        BigDecimal used = compensationRepository.sumApproved(c.getId(), PAYOUT, excludeClaimId);
        return c.getMaxCompensation().subtract(used).max(BigDecimal.ZERO);
    }

    @Override
    public Compensation find(Long id) {
        return compensationRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Không tìm thấy yêu cầu bồi thường"));
    }

    private Compensation findOwned(Long id) {
        Compensation cp = find(id);
        currentUser.checkOwnership(cp.getContract().getCustomer().getId());
        return cp;
    }

    static String vnd(BigDecimal amount) {
        return NumberFormat.getNumberInstance(Locale.forLanguageTag("vi-VN")).format(amount) + " VND";
    }
}
