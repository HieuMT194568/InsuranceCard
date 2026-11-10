package com.insurancecard.service;

import java.time.LocalDateTime;

import com.insurancecard.common.PageResponse;
import com.insurancecard.model.dto.AccidentDtos.AccidentRequest;
import com.insurancecard.model.dto.AccidentDtos.AccidentResolveRequest;
import com.insurancecard.model.dto.AccidentDtos.AccidentResponse;
import com.insurancecard.model.entity.Accident;
import com.insurancecard.model.entity.Contract;
import com.insurancecard.model.enums.AccidentStatus;

public interface AccidentService {

    PageResponse<AccidentResponse> search(String q, Long customerId, Long contractId, AccidentStatus status,
                                          Integer page, Integer size);

    AccidentResponse get(Long id);

    AccidentResponse create(AccidentRequest r);

    /**
     * Tạo bản ghi tai nạn (dùng chung cho khai báo tai nạn và yêu cầu bồi thường kèm khai báo mới).
     * contractField/timeField: tên trường để gắn lỗi lên đúng ô trên form.
     */
    Accident createEntity(AccidentRequest r, String contractField, String timeField);

    AccidentResponse update(Long id, AccidentRequest r);

    /** Nhân viên xác minh / bác bỏ tai nạn. Bác bỏ => các yêu cầu bồi thường đang chờ bị từ chối theo. */
    AccidentResponse resolve(Long id, AccidentResolveRequest r);

    void delete(Long id);

    Accident find(Long id);

    Accident findOwned(Long id);

    /** Tai nạn phải thuộc thời hạn hợp đồng (đang hiệu lực/đã hết hạn) và còn trong hạn khai báo. */
    void validateAgainstContract(Contract c, LocalDateTime accidentTime, String contractField, String timeField);
}
