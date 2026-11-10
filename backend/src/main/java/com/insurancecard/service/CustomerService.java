package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.CustomerDtos.CustomerCreatedResponse;
import com.insurancecard.model.dto.CustomerDtos.CustomerRequest;
import com.insurancecard.model.dto.CustomerDtos.CustomerResponse;
import com.insurancecard.model.dto.CustomerDtos.ProfileResponse;
import com.insurancecard.model.dto.CustomerDtos.ProfileUpdateRequest;
import com.insurancecard.model.dto.CustomerDtos.SetPasswordRequest;
import com.insurancecard.model.dto.CustomerDtos.StaffProfileUpdateRequest;
import com.insurancecard.model.entity.Customer;
import com.insurancecard.model.enums.UserStatus;
import com.insurancecard.security.AppUserPrincipal;

public interface CustomerService {

    ProfileResponse getProfile(AppUserPrincipal me);

    ProfileResponse updateCustomerProfile(AppUserPrincipal me, ProfileUpdateRequest r);

    ProfileResponse updateStaffProfile(AppUserPrincipal me, StaffProfileUpdateRequest r);

    PageResponse<CustomerResponse> search(String q, UserStatus status, Integer page, Integer size);

    CustomerResponse get(Long id);

    CustomerCreatedResponse create(CustomerRequest r);

    CustomerResponse update(Long id, CustomerRequest r);

    /** Khoá / mở khoá / kích hoạt thủ công. Không cho đặt về PENDING. */
    CustomerResponse updateStatus(Long id, UserStatus status);

    /** Nhân viên đặt lại mật khẩu cho khách hàng (khách quên mật khẩu, liên hệ trực tiếp). */
    void setPassword(Long id, SetPasswordRequest r);

    /** Chỉ xoá được khách hàng chưa phát sinh hợp đồng/phạt; ngược lại phải khoá tài khoản. */
    void delete(Long id);

    Customer findCustomer(Long id);
}
