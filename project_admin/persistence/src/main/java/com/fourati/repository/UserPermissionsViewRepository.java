package com.fourati.repository;

import com.fourati.domain.UserPermissionsView;
import com.fourati.domain.UserPermissionsViewId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

/**
 * Read-only repository for {@code user_permissions_view}. Extends the bare
 * {@link Repository} marker interface (not {@link org.springframework.data.jpa.repository.JpaRepository})
 * so no save/delete methods are ever generated against this database view.
 */
public interface UserPermissionsViewRepository extends Repository<UserPermissionsView, UserPermissionsViewId> {

    Page<UserPermissionsView> findAll(Pageable pageable);

    Page<UserPermissionsView> findByUserId(UUID userId, Pageable pageable);
}
