package com.insurancecard.mapper;

import com.insurancecard.model.dto.AuditLogDtos.AuditLogResponse;
import com.insurancecard.model.dto.GarageDtos.GarageResponse;
import com.insurancecard.model.dto.GarageDtos.PublicGarageResponse;
import com.insurancecard.model.dto.StaffAccountDtos.StaffAccountResponse;
import com.insurancecard.model.entity.AuditLog;
import com.insurancecard.model.entity.Garage;
import com.insurancecard.model.entity.User;

/** M02 (Hiếu) – entity -> DTO cho nhân viên, nhật ký, gara. Không bao giờ trả passwordHash. */
public final class AdminMapper {

    private AdminMapper() {}

    public static StaffAccountResponse staff(User u) {
        return new StaffAccountResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole(),
                u.getStatus(), u.getCreatedAt(), u.getUpdatedAt());
    }

    public static AuditLogResponse audit(AuditLog a) {
        User u = a.getUser();
        return new AuditLogResponse(a.getId(), a.getAction(), a.getEntityType(), a.getEntityId(), a.getDescription(),
                a.getIpAddress(), u == null ? null : u.getId(), u == null ? null : u.getEmail(),
                u == null ? null : u.getFullName(), a.getCreatedAt());
    }

    public static GarageResponse garage(Garage g) {
        return new GarageResponse(g.getId(), g.getGarageCode(), g.getName(), g.getAddress(), g.getProvince(),
                g.getPhone(), g.getEmail(), g.getStatus(), g.getNote(), g.getCreatedAt(), g.getUpdatedAt());
    }

    public static PublicGarageResponse publicGarage(Garage g) {
        return new PublicGarageResponse(g.getId(), g.getGarageCode(), g.getName(), g.getAddress(), g.getProvince(),
                g.getPhone(), g.getEmail());
    }
}
