package com.fourati.organization.internal.application;

import com.fourati.organization.OrganizationFacade;
import com.fourati.organization.internal.persistence.OrganizationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
class OrganizationFacadeImpl implements OrganizationFacade {

    private final OrganizationRepository organizationRepository;

    @Override
    public boolean exists(UUID organizationId) {
        return organizationId != null && organizationRepository.existsById(organizationId);
    }
}
