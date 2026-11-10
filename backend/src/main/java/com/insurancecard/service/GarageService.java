package com.insurancecard.service;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.GarageDtos.GarageRequest;
import com.insurancecard.model.dto.GarageDtos.GarageResponse;
import com.insurancecard.model.dto.GarageDtos.PublicGarageResponse;
import com.insurancecard.model.entity.Garage;
import com.insurancecard.model.enums.GarageStatus;

/** M02 – Gara liên kết (UC-43 quản lý, UC-44 xem công khai). Management 5 API. */
public interface GarageService {

    PageResponse<GarageResponse> search(String q, String province, GarageStatus status, Integer page, Integer size);

    GarageResponse get(Long id);

    GarageResponse create(GarageRequest r);

    GarageResponse update(Long id, GarageRequest r);

    void delete(Long id);

    /** Danh sách công khai: chỉ gara ACTIVE. */
    PageResponse<PublicGarageResponse> listPublic(String q, String province, Integer page, Integer size);

    /** Cho M15 (biên bản giám định): lấy gara, 404 nếu không có. */
    Garage find(Long id);
}
