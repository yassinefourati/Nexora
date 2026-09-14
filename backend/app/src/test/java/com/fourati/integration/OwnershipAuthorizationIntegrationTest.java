package com.fourati.integration;

import com.fourati.identity.UserStatus;

import com.fourati.content.internal.domain.Comment;
import com.fourati.notification.internal.domain.Notification;
import com.fourati.identity.internal.domain.User;
import com.fourati.notification.internal.domain.UserNotification;
import com.fourati.content.internal.persistence.CommentRepository;
import com.fourati.notification.internal.persistence.NotificationRepository;
import com.fourati.notification.internal.persistence.UserNotificationRepository;
import com.fourati.identity.internal.persistence.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP-level authorization matrix for user-owned resources, through the real
 * security filter chain, method security and Postgres. This is the test that
 * would have caught the original IDOR: any authenticated user could mark-read
 * or delete any other user's notification and delete any comment.
 *
 * Personas: an admin, "alice" (owns the seeded rows) and "bob" (does not).
 */
class OwnershipAuthorizationIntegrationTest extends AbstractIntegrationTest {

    @Autowired UserRepository userRepository;
    @Autowired NotificationRepository notificationRepository;
    @Autowired UserNotificationRepository userNotificationRepository;
    @Autowired CommentRepository commentRepository;

    private User alice;
    private User bob;
    private Notification notification;
    private UserNotification aliceNotification;
    private Comment aliceComment;

    /** The seeded user, authenticated exactly as the JWT converter would authenticate them. */
    private static RequestPostProcessor as(User user, String... roles) {
        return TestAuthentications.asUser(user, roles);
    }

    /** A person with no backend row (an admin from Keycloak who never touched user-owned data). */
    private static RequestPostProcessor asPerson(String username, String... roles) {
        return TestAuthentications.asPerson(username, roles);
    }

    private User seedUser(String username) {
        User u = new User();
        u.setUsername(username + "-" + UUID.randomUUID());
        u.setEmail(u.getUsername() + "@example.com");
        u.setStatus(UserStatus.ACTIVE);
        return userRepository.save(u);
    }

    @BeforeEach
    void seed() {
        alice = seedUser("alice");
        bob = seedUser("bob");

        notification = new Notification();
        notification.setTitle("t");
        notification.setBody("b");
        notification.setChannel("in_app");
        notification = notificationRepository.save(notification);

        aliceNotification = new UserNotification();
        aliceNotification.setUserId(alice.getId());
        aliceNotification.setNotification(notification);
        aliceNotification.setRead(false);
        aliceNotification.setDeliveredAt(Instant.now());
        aliceNotification = userNotificationRepository.save(aliceNotification);

        aliceComment = new Comment();
        aliceComment.setUserId(alice.getId());
        aliceComment.setEntityType("users");
        aliceComment.setEntityId(alice.getId());
        aliceComment.setBody("hello");
        aliceComment = commentRepository.save(aliceComment);
    }

    @AfterEach
    void cleanup() {
        commentRepository.deleteAll(commentRepository.findByUserId(alice.getId(), org.springframework.data.domain.Pageable.unpaged()));
        userNotificationRepository.findByUserId(alice.getId(), org.springframework.data.domain.Pageable.unpaged())
                .forEach(userNotificationRepository::delete);
        notificationRepository.deleteById(notification.getId());
        userRepository.deleteById(alice.getId());
        userRepository.deleteById(bob.getId());
    }

    private String aliceName() { return alice.getUsername(); }
    private String bobName() { return bob.getUsername(); }

    // --- notifications -------------------------------------------------------

    @Test
    void owner_canMarkOwnNotificationRead() throws Exception {
        mvc.perform(patch("/api/v1/user-notifications/{id}/read", aliceNotification.getId()).with(as(alice, "user")))
           .andExpect(status().isOk());
    }

    @Test
    void otherUser_cannotMarkSomeoneElsesNotificationRead() throws Exception {
        mvc.perform(patch("/api/v1/user-notifications/{id}/read", aliceNotification.getId()).with(as(bob, "user")))
           .andExpect(status().isForbidden());
    }

    @Test
    void otherUser_cannotDeleteSomeoneElsesNotification() throws Exception {
        mvc.perform(delete("/api/v1/user-notifications/{id}", aliceNotification.getId()).with(as(bob, "user")))
           .andExpect(status().isForbidden());
    }

    @Test
    void admin_canActOnAnyNotification() throws Exception {
        mvc.perform(patch("/api/v1/user-notifications/{id}/read", aliceNotification.getId()).with(asPerson("root", "admin")))
           .andExpect(status().isOk());
    }

    @Test
    void otherUser_cannotListSomeoneElsesNotifications() throws Exception {
        mvc.perform(get("/api/v1/user-notifications").param("userId", alice.getId().toString()).with(as(bob, "user")))
           .andExpect(status().isForbidden());
    }

    @Test
    void owner_canListOwnNotifications() throws Exception {
        mvc.perform(get("/api/v1/user-notifications").param("userId", alice.getId().toString()).with(as(alice, "user")))
           .andExpect(status().isOk());
    }

    @Test
    void nonAdmin_cannotAssignNotificationsToOthers() throws Exception {
        String body = """
            {"userId":"%s","notificationId":"%s"}
            """.formatted(bob.getId(), notification.getId());
        mvc.perform(post("/api/v1/user-notifications").contentType(MediaType.APPLICATION_JSON).content(body)
                        .with(as(alice, "user")))
           .andExpect(status().isForbidden());
    }

    // --- comments ------------------------------------------------------------

    @Test
    void otherUser_cannotDeleteSomeoneElsesComment() throws Exception {
        mvc.perform(delete("/api/v1/comments/{id}", aliceComment.getId()).with(as(bob, "user")))
           .andExpect(status().isForbidden());
    }

    @Test
    void author_canDeleteOwnComment() throws Exception {
        mvc.perform(delete("/api/v1/comments/{id}", aliceComment.getId()).with(as(alice, "user")))
           .andExpect(status().isNoContent());
    }

    @Test
    void anonymous_isRejected() throws Exception {
        mvc.perform(get("/api/v1/comments/{id}", aliceComment.getId()))
           .andExpect(status().isUnauthorized());
    }

    // --- metadata: writes are admin-only ---------------------------------------

    @Test
    void nonAdmin_cannotWriteMetadata() throws Exception {
        mvc.perform(delete("/api/v1/metadata/{id}", UUID.randomUUID()).with(as(bob, "user")))
           .andExpect(status().isForbidden());
    }
}
