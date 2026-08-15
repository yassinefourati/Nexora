package com.fourati.repository;

import com.fourati.domain.UserRolesView;
import com.fourati.domain.UserRolesViewId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

/**
 * Read-only repository for {@code user_roles_view}. Extends the bare
 * {@link Repository} marker interface (not {@link org.springframework.data.jpa.repository.JpaRepository})
 * so no save/delete methods are ever generated against this database view.
 */
public interface UserRolesViewRepository extends Repository<UserRolesView, UserRolesViewId> {

    Page<UserRolesView> findAll(Pageable pageable);

    Page<UserRolesView> findByUserId(UUID userId, Pageable pageable);

    Page<UserRolesView> findByRoleId(UUID roleId, Pageable pageable);
}
