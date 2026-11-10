package com.insurancecard.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.insurancecard.model.entity.AuditLog;
import com.insurancecard.model.enums.AuditAction;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query(value = """
            SELECT a FROM AuditLog a LEFT JOIN FETCH a.user u
            WHERE (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:from IS NULL OR a.createdAt >= :from)
              AND (:to IS NULL OR a.createdAt < :to)
              AND (:q IS NULL
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
            """,
            countQuery = """
            SELECT COUNT(a) FROM AuditLog a LEFT JOIN a.user u
            WHERE (:action IS NULL OR a.action = :action)
              AND (:entityType IS NULL OR a.entityType = :entityType)
              AND (:from IS NULL OR a.createdAt >= :from)
              AND (:to IS NULL OR a.createdAt < :to)
              AND (:q IS NULL
                   OR LOWER(a.description) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%')))
            """)
    Page<AuditLog> search(@Param("q") String q, @Param("action") AuditAction action,
                          @Param("entityType") String entityType, @Param("from") LocalDateTime from,
                          @Param("to") LocalDateTime to, Pageable pageable);
}
