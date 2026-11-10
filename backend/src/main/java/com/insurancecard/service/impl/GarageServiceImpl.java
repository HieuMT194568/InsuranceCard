package com.insurancecard.service.impl;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.common.ApiException;
import com.insurancecard.common.PageResponse;
import com.insurancecard.common.Paging;
import com.insurancecard.common.TextNormalizer;
import com.insurancecard.mapper.AdminMapper;
import com.insurancecard.model.dto.GarageDtos.GarageRequest;
import com.insurancecard.model.dto.GarageDtos.GarageResponse;
import com.insurancecard.model.dto.GarageDtos.PublicGarageResponse;
import com.insurancecard.model.entity.Garage;
import com.insurancecard.model.enums.GarageStatus;
import com.insurancecard.repository.GarageRepository;
import com.insurancecard.service.GarageService;
import com.insurancecard.validation.ValidationRules;

@Service
public class GarageServiceImpl implements GarageService {

    private final GarageRepository garageRepository;

    public GarageServiceImpl(GarageRepository garageRepository) {
        this.garageRepository = garageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<GarageResponse> search(String q, String province, GarageStatus status, Integer page,
                                               Integer size) {
        return PageResponse.of(garageRepository.search(Paging.keyword(q), Paging.keyword(province), status,
                Paging.of(page, size)), AdminMapper::garage);
    }

    @Override
    @Transactional(readOnly = true)
    public GarageResponse get(Long id) {
        return AdminMapper.garage(find(id));
    }

    @Override
    @Transactional
    public GarageResponse create(GarageRequest r) {
        String code = normalizeCode(r.garageCode());
        if (garageRepository.existsByGarageCodeIgnoreCase(code)) {
            throw ApiException.duplicate("garageCode", "Mã gara đã tồn tại");
        }
        Garage g = new Garage();
        apply(g, r, code);
        garageRepository.save(g);
        return AdminMapper.garage(g);
    }

    @Override
    @Transactional
    public GarageResponse update(Long id, GarageRequest r) {
        Garage g = find(id);
        String code = normalizeCode(r.garageCode());
        if (garageRepository.existsByGarageCodeIgnoreCaseAndIdNot(code, id)) {
            throw ApiException.duplicate("garageCode", "Mã gara đã tồn tại");
        }
        apply(g, r, code);
        return AdminMapper.garage(g);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Garage g = find(id);
        try {
            garageRepository.delete(g);
            garageRepository.flush();
        } catch (DataIntegrityViolationException e) {
            // claim_assessments (M15) đang trỏ tới gara này (BR-43.2)
            throw ApiException.conflict("GARAGE_IN_USE",
                    "Gara đã được dùng trong biên bản giám định, không thể xoá. Hãy chuyển sang Ngừng hợp tác.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PublicGarageResponse> listPublic(String q, String province, Integer page, Integer size) {
        return PageResponse.of(garageRepository.search(Paging.keyword(q), Paging.keyword(province),
                GarageStatus.ACTIVE, Paging.of(page, size)), AdminMapper::publicGarage);
    }

    @Override
    public Garage find(Long id) {
        return garageRepository.findById(id).orElseThrow(() -> ApiException.notFound("Không tìm thấy gara"));
    }

    private static String normalizeCode(String raw) {
        String code = raw.trim().toUpperCase(Locale.ROOT);
        if (!code.matches(ValidationRules.GARAGE_CODE)) {
            throw ApiException.validation("garageCode", ValidationRules.GARAGE_CODE_MSG);
        }
        return code;
    }

    private static void apply(Garage g, GarageRequest r, String code) {
        g.setGarageCode(code);
        g.setName(collapse(r.name()));
        g.setAddress(collapse(r.address()));
        g.setProvince(collapse(r.province()));
        g.setPhone(r.phone());
        g.setEmail(r.email() == null ? null : TextNormalizer.normalizeEmail(r.email()));
        g.setStatus(r.status());
        g.setNote(r.note());
    }

    private static String collapse(String s) {
        return s == null ? null : s.trim().replaceAll("\\s+", " ");
    }
}
