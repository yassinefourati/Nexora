package com.fourati.integration;

import com.fourati.access.AssignRoleRequest;
import com.fourati.access.internal.application.AssignRoleToUser;
import com.fourati.access.internal.application.RemoveRoleFromUser;
import com.fourati.access.internal.domain.Role;
import com.fourati.access.internal.persistence.RoleRepository;
import com.fourati.access.internal.persistence.UserRoleRepository;
import com.fourati.app.security.AppJwtAuthenticationConverter;
import com.fourati.identity.UserStatus;
import com.fourati.identity.internal.persistence.UserIdentityProviderRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import com.fourati.platform.security.CurrentUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * ADR 0006: the database permission model is the enforced one.
 *
 * A person whose token carries only the coarse {@code user} role is granted the
 * seeded {@code manager} role in the database. Through the real JWT converter
 * they can create a user ({@code user.write}) but not delete one
 * ({@code user.delete}, which managers lack). Once the grant is removed, the
 * very next authentication is denied: the permission cache was cleared by the
 * {@code AccessChanged} event, in the shared store every replica reads.
 */
class PermissionAuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired private AppJwtAuthenticationConverter converter;
    @Autowired private AssignRoleToUser assignRoleToUser;
    @Autowired private RemoveRoleFromUser removeRoleFromUser;
    @Autowired private RoleRepository roleRepository;
    @Autowired private UserRoleRepository userRoleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private UserIdentityProviderRepository identityProviderRepository;

    private String username;
    private String subject;
    private UUID userId;
    private Role manager;

    @BeforeEach
    void provisionManager() {
        username = "perm-" + UUID.randomUUID();
        subject = "sub-" + username;
        manager = roleRepository.findByName("manager").orElseThrow();

        // First authentication provisions the backend row and links the subject.
        userId = principal().userId();

        asAdmin();
        assignRoleToUser.execute(userId, new AssignRoleRequest(manager.getId(), null, null));
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
        userRoleRepository.deleteAll(userRoleRepository.findByUserId(userId));
        identityProviderRepository.findByProviderAndProviderUserId("keycloak", subject)
                .ifPresent(identityProviderRepository::delete);
        userRepository.deleteById(userId);
        userRepository.findByUsername("created-by-" + username).ifPresent(userRepository::delete);
    }

    @Test
    void databaseGrant_isEnforced_evenThoughTheTokenOnlyCarriesTheUserRole() throws Exception {
        assertThat(principal().permissions()).contains("user.write").doesNotContain("user.delete");

        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(createUserBody())
                        .with(asPerson()))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.data.status").value(UserStatus.ACTIVE.value()));
    }

    @Test
    void permissionTheGrantDoesNotCarry_isDenied() throws Exception {
        UUID someone = UUID.randomUUID();

        mvc.perform(delete("/api/v1/users/{id}", someone).with(asPerson()))
           .andExpect(status().isForbidden())
           .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void removedGrant_isDeniedOnTheNextAuthentication() throws Exception {
        assertThat(principal().hasPermission("user.write")).isTrue();

        asAdmin();
        removeRoleFromUser.execute(userId, manager.getId());
        SecurityContextHolder.clearContext();

        assertThat(principal().hasPermission("user.write")).as("cache cleared by AccessChanged").isFalse();
        mvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON).content(createUserBody())
                        .with(asPerson()))
           .andExpect(status().isForbidden());
    }

    // --- helpers -------------------------------------------------------------

    private Jwt jwt() {
        return Jwt.withTokenValue("t").header("alg", "none")
                .subject(subject)
                .claim("preferred_username", username)
                .claim("roles", List.of("user"))
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60))
                .build();
    }

    /** The principal exactly as a real request would build it. */
    private CurrentUser principal() {
        return CurrentUser.from(converter.convert(jwt())).orElseThrow();
    }

    private RequestPostProcessor asPerson() {
        AbstractAuthenticationToken token = converter.convert(jwt());
        return authentication(token);
    }

    private void asAdmin() {
        SecurityContextHolder.getContext().setAuthentication(TestAuthentications.token(null, "root", "admin"));
    }

    private String createUserBody() {
        String created = "created-by-" + username;
        return """
                {"username":"%s","email":"%s@example.com","firstName":"Created","lastName":"ByPermission"}
                """.formatted(created, created);
    }
}
