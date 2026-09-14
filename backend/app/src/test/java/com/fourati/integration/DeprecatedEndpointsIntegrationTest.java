package com.fourati.integration;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Retired join-table write endpoints keep working for one release and announce
 * their retirement with the standard headers (Phase 4 of the plan): clients see
 * {@code Deprecation}, {@code Sunset} and a {@code Link} to the successor on
 * every response, errors included.
 */
class DeprecatedEndpointsIntegrationTest extends AbstractIntegrationTest {

    @Test
    void deprecatedJoinTableWrite_carriesDeprecationSunsetAndSuccessorHeaders_evenOnErrors() throws Exception {
        mvc.perform(delete("/api/v1/user-roles/{id}", UUID.randomUUID())
                        .with(TestAuthentications.asPerson("root", "admin")))
           .andExpect(status().isNotFound())
           .andExpect(header().string("Deprecation", startsWith("@")))
           .andExpect(header().exists("Sunset"))
           .andExpect(header().string("Link", containsString("rel=\"successor-version\"")))
           .andExpect(header().string("Link", containsString("/users/{userId}/roles/{roleId}")));
    }

    @Test
    void readEndpoints_areNotDeprecated() throws Exception {
        mvc.perform(get("/api/v1/user-roles").with(TestAuthentications.asPerson("root", "admin")))
           .andExpect(status().isOk())
           .andExpect(header().doesNotExist("Deprecation"));
    }
}
