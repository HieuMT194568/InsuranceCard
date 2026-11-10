package com.insurancecard.config.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.insurancecard.config.AppProperties;
import com.insurancecard.model.entity.Garage;
import com.insurancecard.model.enums.GarageStatus;
import com.insurancecard.repository.GarageRepository;

/** M02 (Hiếu) – gara liên kết mẫu khi bảng garages còn trống. */
@Component
@Order(11)
public class GarageSeeder implements CommandLineRunner {

    private final AppProperties props;
    private final GarageRepository garages;

    public GarageSeeder(AppProperties props, GarageRepository garages) {
        this.props = props;
        this.garages = garages;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!props.seed().enabled() || garages.count() > 0) {
            return;
        }
        garage("GR-HCM-01", "Honda Head Phát Tiến", "256 Lý Thường Kiệt, Phường 14, Quận 10", "TP. Hồ Chí Minh",
                "0283863456", "phattien@gara.vn", GarageStatus.ACTIVE);
        garage("GR-HCM-02", "Yamaha Town Hùng Cường", "120 Phan Đăng Lưu, Phường 3, Quận Phú Nhuận",
                "TP. Hồ Chí Minh", "0283995123", null, GarageStatus.ACTIVE);
        garage("GR-HN-01", "Sửa xe máy Minh Long", "45 Trần Duy Hưng, Cầu Giấy", "Hà Nội", "0243556789",
                "minhlong@gara.vn", GarageStatus.ACTIVE);
        garage("GR-HN-02", "Honda Ôtô Mỹ Đình (xe máy)", "2 Lê Đức Thọ, Nam Từ Liêm", "Hà Nội", "0243787654",
                null, GarageStatus.ACTIVE);
        garage("GR-DN-01", "Gara Hoà Khánh", "88 Âu Cơ, Liên Chiểu", "Đà Nẵng", "0236373111", null,
                GarageStatus.ACTIVE);
        garage("GR-CT-01", "Tiệm sửa xe Ba Tèo (ngừng hợp tác)", "15 Nguyễn Văn Cừ, Ninh Kiều", "Cần Thơ",
                "0292383222", null, GarageStatus.INACTIVE);
    }

    private void garage(String code, String name, String address, String province, String phone, String email,
                        GarageStatus status) {
        Garage g = new Garage();
        g.setGarageCode(code);
        g.setName(name);
        g.setAddress(address);
        g.setProvince(province);
        g.setPhone(phone);
        g.setEmail(email);
        g.setStatus(status);
        garages.save(g);
    }
}
