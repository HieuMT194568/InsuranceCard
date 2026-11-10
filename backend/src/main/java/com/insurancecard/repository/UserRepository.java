package com.insurancecard.repository;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.insurancecard.model.entity.User;
import com.insurancecard.model.enums.Role;
import com.insurancecard.model.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, Long id);

    long countByRoleAndStatus(Role role, UserStatus status);

    boolean existsByRole(Role role);

    /** M02 – danh sách tài khoản nội bộ (STAFF / ADMIN). */
    @Query("""
            SELECT u FROM User u
            WHERE u.role IN :roles
              AND (:role IS NULL OR u.role = :role)
              AND (:status IS NULL OR u.status = :status)
              AND (:q IS NULL
                   OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
                   OR u.phone LIKE CONCAT('%', :q, '%'))
            """)
    Page<User> searchInternal(@Param("q") String q, @Param("role") Role role, @Param("status") UserStatus status,
                              @Param("roles") Collection<Role> roles, Pageable pageable);
}
