package com.fourati.notification.internal.persistence;

import com.fourati.notification.internal.domain.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID>, JpaSpecificationExecutor<NotificationTemplate> {

    Optional<NotificationTemplate> findByCode(String code);

    boolean existsByCode(String code);
}
