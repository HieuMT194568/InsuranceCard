package com.insurancecard.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.insurancecard.model.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.user.id = :userId AND (:unreadOnly = false OR n.read = false)
            """)
    Page<Notification> findForUser(@Param("userId") Long userId, @Param("unreadOnly") boolean unreadOnly,
                                   Pageable pageable);

    long countByUserIdAndReadFalse(Long userId);

    boolean existsByUserIdAndRefKey(Long userId, String refKey);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true, n.readAt = :now WHERE n.user.id = :userId AND n.read = false")
    int markAllRead(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}
