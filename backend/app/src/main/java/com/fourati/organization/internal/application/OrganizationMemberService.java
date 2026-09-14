package com.fourati.organization.internal.application;

import com.fourati.identity.IdentityFacade;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.organization.internal.domain.Department;
import com.fourati.organization.internal.domain.Organization;
import com.fourati.organization.internal.domain.OrganizationMember;
import com.fourati.organization.internal.domain.Team;
import com.fourati.identity.internal.domain.User;
import com.fourati.organization.CreateOrganizationMemberRequest;
import com.fourati.organization.OrganizationMemberResponse;
import com.fourati.organization.internal.persistence.DepartmentRepository;
import com.fourati.organization.internal.persistence.OrganizationMemberRepository;
import com.fourati.organization.internal.persistence.OrganizationRepository;
import com.fourati.organization.internal.persistence.TeamRepository;
import com.fourati.platform.audit.Audited;
import com.fourati.platform.error.ConflictException;
import com.fourati.platform.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrganizationMemberService {

    private final OrganizationMemberRepository organizationMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final IdentityFacade identity;
    private final DepartmentRepository departmentRepository;
    private final TeamRepository teamRepository;
    private final OrganizationMemberMapper organizationMemberMapper;

    @Audited(action = "CREATE", description = "Add a member to an organization")
    public OrganizationMemberResponse create(CreateOrganizationMemberRequest request) {
        if (organizationMemberRepository.existsByOrganizationIdAndUserId(request.organizationId(), request.userId())) {
            throw new ConflictException("User " + request.userId()
                    + " is already a member of organization " + request.organizationId());
        }
        Organization organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization", request.organizationId()));
        identity.ensureExists(request.userId());

        OrganizationMember entity = organizationMemberMapper.toEntity(request);
        entity.setOrganization(organization);
        entity.setUserId(request.userId());
        if (request.departmentId() != null) {
            entity.setDepartment(getDepartmentOrThrow(request.departmentId()));
        }
        if (request.teamId() != null) {
            entity.setTeam(getTeamOrThrow(request.teamId()));
        }
        entity.setJoinedAt(Instant.now());
        OrganizationMember saved = organizationMemberRepository.save(entity);
        return organizationMemberMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OrganizationMemberResponse findById(UUID id) {
        return organizationMemberMapper.toResponse(getEntityOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<OrganizationMemberResponse> findAll(Pageable pageable) {
        return organizationMemberRepository.findAll(pageable).map(organizationMemberMapper::toResponse);
    }

    /** Server-side search over title (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<OrganizationMemberResponse> search(String q, Pageable pageable) {
        return organizationMemberRepository.findAll(TextSearch.<OrganizationMember>contains(q, "title"), pageable).map(organizationMemberMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> findByOrganizationId(UUID organizationId) {
        return organizationMemberRepository.findByOrganizationId(organizationId).stream()
                .map(organizationMemberMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> findByUserId(UUID userId) {
        return organizationMemberRepository.findByUserId(userId).stream()
                .map(organizationMemberMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> findByDepartmentId(UUID departmentId) {
        return organizationMemberRepository.findByDepartmentId(departmentId).stream()
                .map(organizationMemberMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrganizationMemberResponse> findByTeamId(UUID teamId) {
        return organizationMemberRepository.findByTeamId(teamId).stream()
                .map(organizationMemberMapper::toResponse)
                .toList();
    }

    @Audited(action = "DELETE", description = "Remove a member from an organization")
    public void delete(UUID id) {
        OrganizationMember entity = getEntityOrThrow(id);
        organizationMemberRepository.delete(entity);
    }

    private OrganizationMember getEntityOrThrow(UUID id) {
        return organizationMemberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OrganizationMember", id));
    }

    private Department getDepartmentOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }

    private Team getTeamOrThrow(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }
}
