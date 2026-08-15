package com.fourati.repository;

import com.fourati.domain.ActiveUsersView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.Repository;

import java.util.UUID;

/**
 * Read-only repository for {@code active_users_view}. Extends the bare
 * {@link Repository} marker interface (not {@link org.springframework.data.jpa.repository.JpaRepository})
 * so no save/delete methods are ever generated against this database view.
 */
public interface ActiveUsersViewRepository extends Repository<ActiveUsersView, UUID> {

    Page<ActiveUsersView> findAll(Pageable pageable);

    Page<ActiveUsersView> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
}
