package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.VehicleDtos.VehicleRequest;
import com.insurancecard.model.dto.VehicleDtos.VehicleResponse;
import com.insurancecard.model.entity.Vehicle;

public interface VehicleService {

    PageResponse<VehicleResponse> search(String q, Long customerId, Integer page, Integer size);

    VehicleResponse get(Long id);

    VehicleResponse create(VehicleRequest r);

    VehicleResponse update(Long id, VehicleRequest r);

    void delete(Long id);

    /** Lấy xe và kiểm tra quyền sở hữu (khách hàng chỉ thao tác xe của mình). */
    Vehicle findOwned(Long id);
}
