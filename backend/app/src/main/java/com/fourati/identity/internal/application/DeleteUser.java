package com.fourati.identity.internal.application;

import com.fourati.shared.security.Authorize;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fourati.identity.IdentityChanged;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/** Use case: soft-delete a user. The row stays for audit history but disappears from every read path. */
@Service
@RequiredArgsConstructor
@Transactional
public class DeleteUser {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @PreAuthorize(Authorize.USER_DELETE)
    @Audited(action = "DELETE", entityType = "user", description = "Soft-delete a user")
    public void execute(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.softDelete(Instant.now());
        userRepository.save(user);
        eventPublisher.publishEvent(new IdentityChanged(id, "DELETED"));
    }
}
