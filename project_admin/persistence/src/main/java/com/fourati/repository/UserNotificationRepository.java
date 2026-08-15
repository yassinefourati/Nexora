package com.fourati.repository;

import com.fourati.domain.UserNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserNotificationRepository extends JpaRepository<UserNotification, UUID> {

    Page<UserNotification> findByUserId(UUID userId, Pageable pageable);

    Page<UserNotification> findByUserIdAndRead(UUID userId, boolean read, Pageable pageable);

    Optional<UserNotification> findByUserIdAndNotificationId(UUID userId, UUID notificationId);

    boolean existsByUserIdAndNotificationId(UUID userId, UUID notificationId);

    long countByUserIdAndRead(UUID userId, boolean read);
}
