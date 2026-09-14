package com.fourati.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The software bill of materials ships with the build (ADR 0008, Phase 6):
 * the CycloneDX plugin writes {@code META-INF/sbom/application.cdx.json} into
 * the jar, the Dockerfile copies it next to the jar, and the actuator serves
 * it. Like the metrics scrape it is anonymous only on the management port,
 * which MockMvc never simulates: on the public port it needs a caller.
 */
class SbomContractIntegrationTest extends AbstractIntegrationTest {

    @Test
    void sbom_onThePublicPort_requiresAuthentication() throws Exception {
        mvc.perform(get("/actuator/sbom"))
           .andExpect(status().isUnauthorized())
           .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
           .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
           .andExpect(jsonPath("$.instance").value("/actuator/sbom"));
    }

    @Test
    void sbom_isTheCycloneDxDocumentGeneratedByTheBuild() throws Exception {
        mvc.perform(get("/actuator/sbom").with(TestAuthentications.asPerson("root", "admin")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.ids[0]").value("application"));

        mvc.perform(get("/actuator/sbom/application").with(TestAuthentications.asPerson("root", "admin")))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.bomFormat").value("CycloneDX"))
           .andExpect(jsonPath("$.metadata.component.name").value("app"))
           .andExpect(jsonPath("$.components[?(@.name == 'spring-boot')]").exists());
    }
}
