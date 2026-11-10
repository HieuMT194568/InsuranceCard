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
import com.insurancecard.model.dto.GarageDtos.GarageRequest;
import com.insurancecard.model.dto.GarageDtos.GarageResponse;
import com.insurancecard.model.dto.GarageDtos.PublicGarageResponse;
import com.insurancecard.model.enums.GarageStatus;
import com.insurancecard.service.GarageService;

import jakarta.validation.Valid;

/** M02 (Hiếu) – UC-43 Quản lý gara liên kết (5 API, nhân viên) + UC-44 danh sách công khai. */
@RestController
@RequestMapping("/api")
public class GarageController {

    private final GarageService garageService;

    public GarageController(GarageService garageService) {
        this.garageService = garageService;
    }

    @GetMapping("/garages")
    @PreAuthorize("hasRole('STAFF')")
    public PageResponse<GarageResponse> search(@RequestParam(required = false) String q,
                                               @RequestParam(required = false) String province,
                                               @RequestParam(required = false) GarageStatus status,
                                               @RequestParam(required = false) Integer page,
                                               @RequestParam(required = false) Integer size) {
        return garageService.search(q, province, status, page, size);
    }

    @GetMapping("/garages/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public GarageResponse get(@PathVariable Long id) {
        return garageService.get(id);
    }

    @PostMapping("/garages")
    @PreAuthorize("hasRole('STAFF')")
    @ResponseStatus(HttpStatus.CREATED)
    public GarageResponse create(@Valid @RequestBody GarageRequest request) {
        return garageService.create(request);
    }

    @PutMapping("/garages/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public GarageResponse update(@PathVariable Long id, @Valid @RequestBody GarageRequest request) {
        return garageService.update(id, request);
    }

    @DeleteMapping("/garages/{id}")
    @PreAuthorize("hasRole('STAFF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        garageService.delete(id);
    }

    /** Công khai (không cần đăng nhập): chỉ gara đang hợp tác. */
    @GetMapping("/public/garages")
    public PageResponse<PublicGarageResponse> listPublic(@RequestParam(required = false) String q,
                                                         @RequestParam(required = false) String province,
                                                         @RequestParam(required = false) Integer page,
                                                         @RequestParam(required = false) Integer size) {
        return garageService.listPublic(q, province, page, size);
    }
}
