package com.insurancecard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.CompensationDtos.ApproveRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimRequest;
import com.insurancecard.model.dto.CompensationDtos.ClaimUpdateRequest;
import com.insurancecard.model.dto.CompensationDtos.CompensationResponse;
import com.insurancecard.model.dto.CompensationDtos.PayoutRequest;
import com.insurancecard.model.dto.CompensationDtos.RejectRequest;
import com.insurancecard.model.enums.ClaimStatus;
import com.insurancecard.service.CompensationService;

import jakarta.validation.Valid;

/** Khách hàng: tạo / sửa / rút yêu cầu của mình khi còn chờ duyệt. Nhân viên: thêm duyệt, từ chối, chi trả. */
@RestController
@RequestMapping("/api/compensations")
public class CompensationController {

    private final CompensationService compensationService;

    public CompensationController(CompensationService compensationService) {
        this.compensationService = compensationService;
    }

    @GetMapping
    public PageResponse<CompensationResponse> search(@RequestParam(required = false) String q,
                                                     @RequestParam(required = false) Long customerId,
                                                     @RequestParam(required = false) ClaimStatus status,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        return compensationService.search(q, customerId, status, page, size);
    }

    @GetMapping("/{id}")
    public CompensationResponse get(@PathVariable Long id) {
        return compensationService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompensationResponse create(@Valid @RequestBody ClaimRequest request) {
        return compensationService.create(request);
    }

    @PutMapping("/{id}")
    public CompensationResponse update(@PathVariable Long id, @Valid @RequestBody ClaimUpdateRequest request) {
        return compensationService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        compensationService.delete(id);
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('STAFF')")
    public CompensationResponse approve(@PathVariable Long id, @Valid @RequestBody ApproveRequest request) {
        return compensationService.approve(id, request);
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('STAFF')")
    public CompensationResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return compensationService.reject(id, request);
    }

    @PostMapping("/{id}/payout")
    @PreAuthorize("hasRole('STAFF')")
    public CompensationResponse payout(@PathVariable Long id, @Valid @RequestBody PayoutRequest request) {
        return compensationService.payout(id, request);
    }
}
