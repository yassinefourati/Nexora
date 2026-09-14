package com.fourati.organization.internal.application;

import com.fourati.shared.persistence.TextSearch;

import com.fourati.shared.persistence.VersionCheck;

import com.fourati.organization.internal.domain.Department;
import com.fourati.organization.internal.domain.Organization;
import com.fourati.organization.internal.domain.Team;
import com.fourati.organization.CreateTeamRequest;
import com.fourati.organization.UpdateTeamRequest;
import com.fourati.organization.TeamResponse;
import com.fourati.organization.internal.persistence.DepartmentRepository;
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
public class TeamService {

    private final TeamRepository teamRepository;
    private final OrganizationRepository organizationRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamMapper teamMapper;

    @Audited(action = "CREATE", description = "Create a new team")
    public TeamResponse create(CreateTeamRequest request) {
        if (teamRepository.existsByOrganizationIdAndName(request.organizationId(), request.name())) {
            throw new ConflictException("Team already exists with name: " + request.name()
                    + " in organization: " + request.organizationId());
        }
        Organization organization = getOrganizationOrThrow(request.organizationId());
        Team entity = teamMapper.toEntity(request);
        entity.setOrganization(organization);
        if (request.departmentId() != null) {
            entity.setDepartment(getDepartmentOrThrow(request.departmentId()));
        }
        Team saved = teamRepository.save(entity);
        return teamMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public TeamResponse findById(UUID id) {
        return teamMapper.toResponse(getTeamOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Page<TeamResponse> findAll(Pageable pageable) {
        return teamRepository.findAll(pageable).map(teamMapper::toResponse);
    }

    /** Server-side search over name (case-insensitive contains); blank q lists everything. */
    @Transactional(readOnly = true)
    public Page<TeamResponse> search(String q, Pageable pageable) {
        return teamRepository.findAll(TextSearch.<Team>contains(q, "name"), pageable).map(teamMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findByOrganizationId(UUID organizationId) {
        return teamRepository.findByOrganizationId(organizationId).stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamResponse> findByDepartmentId(UUID departmentId) {
        return teamRepository.findByDepartmentId(departmentId).stream()
                .map(teamMapper::toResponse)
                .toList();
    }

    @Audited(action = "UPDATE", description = "Update a team")
    public TeamResponse update(UUID id, UpdateTeamRequest request) {
        Team entity = getTeamOrThrow(id);
        VersionCheck.check(Team.class, id, entity.getVersion(), request.version());
        if (!entity.getName().equals(request.name())
                && teamRepository.existsByOrganizationIdAndName(entity.getOrganization().getId(), request.name())) {
            throw new ConflictException("Team already exists with name: " + request.name()
                    + " in organization: " + entity.getOrganization().getId());
        }
        teamMapper.updateEntityFromRequest(request, entity);
        if (request.departmentId() != null) {
            entity.setDepartment(getDepartmentOrThrow(request.departmentId()));
        } else {
            entity.setDepartment(null);
        }
        Team saved = teamRepository.save(entity);
        return teamMapper.toResponse(saved);
    }

    @Audited(action = "DELETE", description = "Soft-delete a team")
    public void delete(UUID id) {
        Team entity = getTeamOrThrow(id);
        entity.setDeletedAt(Instant.now());
        teamRepository.save(entity);
    }

    private Team getTeamOrThrow(UUID id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team", id));
    }

    private Organization getOrganizationOrThrow(UUID id) {
        return organizationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization", id));
    }

    private Department getDepartmentOrThrow(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department", id));
    }
}
