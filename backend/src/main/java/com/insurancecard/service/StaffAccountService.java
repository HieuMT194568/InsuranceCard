package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreateRequest;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountCreatedResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountUpdateRequest;
import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;

/** M02 – Quản lý tài khoản nhân viên / quản trị viên (UC-24). Management 5 API. */
public interface StaffAccountService {

    PageResponse<StaffAccountResponse> search(String q, Role role, UserStatus status, Integer page, Integer size);

    StaffAccountResponse get(Long id);

    StaffAccountCreatedResponse create(StaffAccountCreateRequest r);

    StaffAccountResponse update(Long id, StaffAccountUpdateRequest r);

    void delete(Long id);
}
