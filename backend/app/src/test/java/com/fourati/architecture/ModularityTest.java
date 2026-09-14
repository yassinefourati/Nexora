package com.fourati.architecture;

import com.fourati.AdminApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

/**
 * Spring Modulith boundary verification (ADR 0004). Every top-level package
 * under {@code com.fourati} is an application module: the business domains
 * ({@code identity}, {@code access}, {@code organization}, {@code configuration},
 * {@code content}, {@code notification}, {@code audit}), the application shell
 * ({@code app}) and the open cross-cutting modules ({@code platform},
 * {@code shared}). A module may only use another module's public API (its
 * root package, or an open module) and the module graph must be acyclic.
 */
class ModularityTest {

    static final ApplicationModules MODULES = ApplicationModules.of(AdminApiApplication.class);

    @Test
    void modulesRespectTheirBoundaries() {
        MODULES.verify();
    }

    @Test
    void writesModuleDocumentation() {
        // target/spring-modulith-docs: one PlantUML component diagram per module plus the module canvases.
        new Documenter(MODULES).writeDocumentation();
    }
}
