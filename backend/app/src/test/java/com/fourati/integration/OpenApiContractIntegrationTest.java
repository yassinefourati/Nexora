package com.fourati.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pins the generated OpenAPI document to a committed snapshot so an API change
 * is a visible diff in review, never an accident discovered by the frontend.
 *
 * <ul>
 *   <li>Normal run: the live {@code /v3/api-docs} must equal
 *       {@code src/test/resources/contract/openapi.json} (JSON-tree comparison,
 *       key order ignored). On mismatch the live document is written to
 *       {@code target/openapi.actual.json} for diffing.</li>
 *   <li>Intentional change: run with {@code -Dopenapi.update=true} to rewrite
 *       the snapshot, then commit it with the code change.</li>
 *   <li>First run: a missing snapshot is created and the test passes.</li>
 * </ul>
 */
class OpenApiContractIntegrationTest extends AbstractIntegrationTest {

    private static final Path SNAPSHOT = Path.of(System.getProperty("openapi.snapshot",
            "src/test/resources/contract/openapi.json"));
    private static final Path ACTUAL = Path.of("target", "openapi.actual.json");

    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    void openApiDocument_matchesCommittedSnapshot() throws Exception {
        String live = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode actual = mapper.readTree(live);
        String pretty = mapper.writeValueAsString(actual) + System.lineSeparator();

        boolean update = Boolean.getBoolean("openapi.update");
        if (update || !Files.exists(SNAPSHOT)) {
            Files.createDirectories(SNAPSHOT.getParent());
            Files.writeString(SNAPSHOT, pretty, StandardCharsets.UTF_8);
            return;
        }

        JsonNode expected = mapper.readTree(Files.readString(SNAPSHOT, StandardCharsets.UTF_8));
        if (!expected.equals(actual)) {
            Files.createDirectories(ACTUAL.getParent());
            Files.writeString(ACTUAL, pretty, StandardCharsets.UTF_8);
        }
        assertThat(actual)
                .withFailMessage("""
                        The OpenAPI document changed. If this is intended, refresh the snapshot with
                          ./mvnw test -pl test -am -Dtest=OpenApiContractIntegrationTest -Dopenapi.update=true
                        and commit %s together with the code change.
                        The live document was written to %s for diffing.""",
                        SNAPSHOT, ACTUAL.toAbsolutePath())
                .isEqualTo(expected);
    }
}
