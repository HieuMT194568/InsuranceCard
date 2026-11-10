package com.insurancecard.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.CodeGenerator;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.mapper.DtoMapper;
import com.insurancecard.model.dto.AccidentDtos.AccidentRequest;
import com.insurancecard.model.dto.AccidentDtos.AccidentResolveRequest;
import com.insurancecard.model.dto.AccidentDtos.AccidentResponse;
import com.insurancecard.model.entity.Accident;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.enums.AccidentStatus;
import com.insurancecard.model.enums.ClaimStatus;
import com.insurancecard.model.enums.ContractStatus;
import com.insurancecard.repository.AccidentRepository;
import com.insurancecard.repository.CompensationRepository;
import com.insurancecard.repository.UserRepository;
import com.insurancecard.security.AppUserPrincipal;
import com.insurancecard.security.CurrentUser;
import com.insurancecard.service.AccidentService;
import com.insurancecard.service.ContractService;
import com.insurancecard.service.SettingService;

@Service
public class AccidentServiceImpl implements AccidentService {

    private static final DateTimeFormatter VN_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final AccidentRepository accidentRepository;
    private final CompensationRepository compensationRepository;
    private final UserRepository userRepository;
    private final ContractService contractService;
    private final SettingService settingService;
    private final CurrentUser currentUser;

    public AccidentServiceImpl(AccidentRepository accidentRepository, CompensationRepository compensationRepository,
                           UserRepository userRepository, ContractService contractService,
                           SettingService settingService, CurrentUser currentUser) {
        this.accidentRepository = accidentRepository;
        this.compensationRepository = compensationRepository;
        this.userRepository = userRepository;
        this.contractService = contractService;
        this.settingService = settingService;
        this.currentUser = currentUser;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AccidentResponse> search(String q, Long customerId, Long contractId, AccidentStatus status,
                                                 Integer page, Integer size) {
        return PageResponse.of(accidentRepository.search(Paging.keyword(q), currentUser.scopeCustomerId(customerId),
                contractId, status, Paging.of(page, size)), DtoMapper::accident);
    }

    @Override
    @Transactional(readOnly = true)
    public AccidentResponse get(Long id) {
        return DtoMapper.accident(findOwned(id));
    }

    @Override
    @Transactional
    public AccidentResponse create(AccidentRequest r) {
        return DtoMapper.accident(createEntity(r, "contractId", "accidentTime"));
    }

    @Override
    public Accident createEntity(AccidentRequest r, String contractField, String timeField) {
        AppUserPrincipal me = currentUser.get();
        Contract contract = contractService.findOwned(r.contractId());
        validateAgainstContract(contract, r.accidentTime(), contractField, timeField);

        Accident a = new Accident();
        a.setAccidentCode(CodeGenerator.next("TN", accidentRepository::existsByAccidentCode));
        a.setContract(contract);
        apply(a, r);
        a.setStatus(AccidentStatus.REPORTED);
        a.setReportedBy(userRepository.getReferenceById(me.userId()));
        return accidentRepository.save(a);
    }

    @Override
    @Transactional
    public AccidentResponse update(Long id, AccidentRequest r) {
        Accident a = findOwned(id);
        if (a.getStatus() != AccidentStatus.REPORTED) {
            throw ApiException.business("ACCIDENT_RESOLVED", "Tai nạn đã được xử lý, không thể chỉnh sửa");
        }
        Contract contract = contractService.findOwned(r.contractId());
        if (!contract.getId().equals(a.getContract().getId()) && compensationRepository.existsByAccidentId(id)) {
            throw ApiException.businessField("ACCIDENT_HAS_CLAIM", "contractId",
                    "Tai nạn đã có yêu cầu bồi thường, không được đổi hợp đồng");
        }
        validateAgainstContract(contract, r.accidentTime(), "contractId", "accidentTime");
        a.setContract(contract);
        apply(a, r);
        return DtoMapper.accident(a);
    }

    @Override
    @Transactional
    public AccidentResponse resolve(Long id, AccidentResolveRequest r) {
        Accident a = find(id);
        if (r.status() != AccidentStatus.VERIFIED && r.status() != AccidentStatus.REJECTED) {
            throw ApiException.validation("status", "Kết quả xử lý chỉ được là VERIFIED (xác minh) hoặc REJECTED (bác bỏ)");
        }
        if (a.getStatus() != AccidentStatus.REPORTED) {
            throw ApiException.business("ACCIDENT_RESOLVED", "Tai nạn đã được xử lý trước đó");
        }
        if (r.status() == AccidentStatus.REJECTED && (r.note() == null || r.note().length() < 10)) {
            throw ApiException.validation("note", "Vui lòng nhập lý do bác bỏ (10-500 ký tự)");
        }
        LocalDateTime now = LocalDateTime.now();
        var staff = userRepository.getReferenceById(currentUser.get().userId());
        a.setStatus(r.status());
        a.setResolutionNote(r.note());
        a.setResolvedAt(now);
        a.setResolvedBy(staff);
        if (r.status() == AccidentStatus.REJECTED) {
            compensationRepository.findByAccidentIdAndStatus(id, ClaimStatus.PENDING).forEach(cp -> {
                cp.setStatus(ClaimStatus.REJECTED);
                cp.setResolutionNote("Tai nạn không được xác minh: " + r.note());
                cp.setResolvedAt(now);
                cp.setResolvedBy(staff);
            });
        }
        return DtoMapper.accident(a);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Accident a = findOwned(id);
        if (a.getStatus() != AccidentStatus.REPORTED) {
            throw ApiException.business("ACCIDENT_RESOLVED", "Chỉ xoá được tai nạn đang chờ xử lý");
        }
        if (compensationRepository.existsByAccidentId(id)) {
            throw ApiException.conflict("ACCIDENT_HAS_CLAIM", "Tai nạn đã có yêu cầu bồi thường, không thể xoá");
        }
        accidentRepository.delete(a);
    }

    @Override
    public Accident find(Long id) {
        return accidentRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy tai nạn"));
    }

    @Override
    public Accident findOwned(Long id) {
        Accident a = find(id);
        currentUser.checkOwnership(a.getContract().getCustomer().getId());
        return a;
    }

    @Override
    public void validateAgainstContract(Contract c, LocalDateTime accidentTime, String contractField, String timeField) {
        if (c.getStatus() != ContractStatus.ACTIVE && c.getStatus() != ContractStatus.EXPIRED) {
            throw ApiException.businessField("CONTRACT_NOT_EFFECTIVE", contractField,
                    "Chỉ khai báo tai nạn cho hợp đồng đang hiệu lực hoặc đã hết hạn");
        }
        LocalDate date = accidentTime.toLocalDate();
        if (accidentTime.isAfter(LocalDateTime.now())) {
            throw ApiException.validation(timeField, "Thời điểm tai nạn không được ở tương lai");
        }
        if (date.isBefore(c.getStartDate()) || date.isAfter(c.getEndDate())) {
            throw ApiException.businessField("OUT_OF_COVERAGE", timeField,
                    "Thời điểm tai nạn phải nằm trong thời hạn hợp đồng (" + c.getStartDate().format(VN_DATE)
                            + " - " + c.getEndDate().format(VN_DATE) + ")");
        }
        int deadline = settingService.getInt(SettingService.CLAIM_DEADLINE_DAYS);
        if (date.plusDays(deadline).isBefore(LocalDate.now())) {
            throw ApiException.businessField("REPORT_DEADLINE_PASSED", timeField,
                    "Đã quá thời hạn khai báo " + deadline + " ngày kể từ ngày xảy ra tai nạn");
        }
    }

    private static void apply(Accident a, AccidentRequest r) {
        a.setAccidentTime(r.accidentTime().withNano(0));
        a.setLocation(r.location());
        a.setDescription(r.description());
        a.setDamageType(r.damageType());
        a.setEstimatedDamage(r.estimatedDamage());
        a.setPoliceReportNumber(r.policeReportNumber());
    }
}
