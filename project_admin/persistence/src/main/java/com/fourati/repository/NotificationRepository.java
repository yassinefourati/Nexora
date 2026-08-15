package com.fourati.repository;

import com.fourati.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByChannel(String channel, Pageable pageable);

    Page<Notification> findByTemplateId(UUID templateId, Pageable pageable);
}
