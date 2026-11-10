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
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreateRequest;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreatedResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountUpdateRequest;
import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;
import com.insurancecard.service.StaffAccountService;

import jakarta.validation.Valid;

/** M02 (Hiếu) – UC-24 Quản lý tài khoản nhân viên. Management 5 API, chỉ ADMIN. */
@RestController
@RequestMapping("/api/staff-accounts")
@PreAuthorize("hasRole('ADMIN')")
public class StaffAccountController {

    private final StaffAccountService staffAccountService;

    public StaffAccountController(StaffAccountService staffAccountService) {
        this.staffAccountService = staffAccountService;
    }

    @GetMapping
    public PageResponse<StaffAccountResponse> search(@RequestParam(required = false) String q,
                                                     @RequestParam(required = false) Role role,
                                                     @RequestParam(required = false) UserStatus status,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer size) {
        return staffAccountService.search(q, role, status, page, size);
    }

    @GetMapping("/{id}")
    public StaffAccountResponse get(@PathVariable Long id) {
        return staffAccountService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StaffAccountCreatedResponse create(@Valid @RequestBody StaffAccountCreateRequest request) {
        return staffAccountService.create(request);
    }

    @PutMapping("/{id}")
    public StaffAccountResponse update(@PathVariable Long id, @Valid @RequestBody StaffAccountUpdateRequest request) {
        return staffAccountService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        staffAccountService.delete(id);
    }
}
