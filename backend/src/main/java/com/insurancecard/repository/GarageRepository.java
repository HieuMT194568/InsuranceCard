package com.insurancecard.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.insurancecard.model.entity.Garage;
import com.insurancecard.model.enums.GarageStatus;

public interface GarageRepository extends JpaRepository<Garage, Long> {

    boolean existsByGarageCodeIgnoreCase(String garageCode);

    boolean existsByGarageCodeIgnoreCaseAndIdNot(String garageCode, Long id);

    @Query("""
            SELECT g FROM Garage g
            WHERE (:status IS NULL OR g.status = :status)
              AND (:province IS NULL OR LOWER(g.province) LIKE LOWER(CONCAT('%', :province, '%')))
              AND (:q IS NULL
                   OR LOWER(g.garageCode) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(g.name) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(g.address) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR g.phone LIKE CONCAT('%', :q, '%'))
            """)
    Page<Garage> search(@Param("q") String q, @Param("province") String province,
                        @Param("status") GarageStatus status, Pageable pageable);
}
