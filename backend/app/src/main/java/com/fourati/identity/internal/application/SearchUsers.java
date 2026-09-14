package com.fourati.identity.internal.application;

import com.fourati.identity.UserResponse;
import com.fourati.identity.UserStatus;
import com.fourati.identity.internal.domain.User;
import com.fourati.identity.internal.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Query: paged user search. {@code q} matches username, email, first or last
 * name (case-insensitive, contains); {@code status} filters exactly. Either
 * may be null. An unknown status value is a 400, not an empty page.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchUsers {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public Page<UserResponse> execute(String q, String status, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();
        if (q != null && !q.isBlank()) {
            String like = "%" + q.trim().toLowerCase() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("username")), like),
                    cb.like(cb.lower(root.get("email").as(String.class)), like),
                    cb.like(cb.lower(cb.coalesce(root.get("firstName"), "")), like),
                    cb.like(cb.lower(cb.coalesce(root.get("lastName"), "")), like)));
        }
        if (status != null && !status.isBlank()) {
            UserStatus wanted = UserStatus.fromValue(status);
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), wanted));
        }
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }
}
