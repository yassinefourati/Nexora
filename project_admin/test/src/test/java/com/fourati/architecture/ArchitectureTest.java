package com.fourati.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces the layered architecture contract at build time.
 *
 * Rules here catch violations early — before code review and before production.
 * Add rules as the project grows; never relax existing ones without team agreement.
 *
 * Architecture:
 *   API (controllers, DTOs) → Service → Repository → Domain
 *
 * Key invariants:
 *   - Controllers must not call repositories directly (bypass the service layer)
 *   - Services must not depend on API DTOs (prevents controller/service coupling)
 *   - Domain objects are plain Java — no Spring, no JPA (until JPA is explicitly added)
 *   - Repositories must not depend on the service or API layers
 */
class ArchitectureTest {

    private static final String BASE = "com.fourati";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);
    }

    @Test
    void controllers_must_not_directly_access_repositories() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".api..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".repository..")
            .because("controllers must delegate to services, not call repositories directly");
        rule.check(classes);
    }

    @Test
    void services_must_not_depend_on_api_layer() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".service..")
            .should().dependOnClassesThat().resideInAPackage(BASE + ".api..")
            .because("services must not import controllers or API-layer classes");
        rule.check(classes);
    }

    @Test
    void repositories_must_not_depend_on_service_or_api() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".repository..")
            .should().dependOnClassesThat()
                .resideInAnyPackage(BASE + ".service..", BASE + ".api..")
            .because("repositories are the lowest layer — no upward dependencies allowed");
        rule.check(classes);
    }

    @Test
    void domain_classes_must_not_have_spring_annotations() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".domain..")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Service")
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .because("domain objects are plain Java — free of framework coupling");
        rule.check(classes);
    }

    @Test
    void controllers_must_be_annotated_with_RestController() {
        ArchRule rule = classes()
            .that().resideInAPackage(BASE + ".api..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .because("all controllers must be Spring REST controllers");
        rule.check(classes);
    }

    @Test
    void services_must_be_annotated_with_Service() {
        ArchRule rule = classes()
            .that().resideInAPackage(BASE + ".service..")
            .and().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .because("all service classes must be Spring-managed beans");
        rule.check(classes);
    }

    @Test
    void mutating_controller_endpoints_must_declare_preAuthorize() {
        List<String> mutatingMappings = List.of(
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.PatchMapping",
            "org.springframework.web.bind.annotation.DeleteMapping"
        );

        ArchRule rule = methods()
            .that().areDeclaredInClassesThat().resideInAPackage(BASE + ".api..")
            .and().areAnnotatedWith(mutatingMappings.get(0))
                .or().areAnnotatedWith(mutatingMappings.get(1))
                .or().areAnnotatedWith(mutatingMappings.get(2))
                .or().areAnnotatedWith(mutatingMappings.get(3))
            .should(beAuthorizedAtMethodOrClassLevel())
            .because("every mutating endpoint (POST/PUT/PATCH/DELETE) must declare @PreAuthorize, either on "
                + "the method itself or on the containing controller class — this is exactly the class of gap "
                + "that let 6 controllers ship with no role check on their write endpoints");
        rule.check(classes);
    }

    private static ArchCondition<JavaMethod> beAuthorizedAtMethodOrClassLevel() {
        String preAuthorize = "org.springframework.security.access.prepost.PreAuthorize";
        return new ArchCondition<>("be annotated with @PreAuthorize, or belong to a class annotated with it") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                boolean authorized = method.isAnnotatedWith(preAuthorize)
                    || method.getOwner().isAnnotatedWith(preAuthorize);
                if (!authorized) {
                    events.add(SimpleConditionEvent.violated(method,
                        method.getFullName() + " is a mutating endpoint with no @PreAuthorize "
                            + "at the method or class level"));
                }
            }
        };
    }

    @Test
    void platform_classes_must_not_depend_on_domain_code() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".platform..")
            .should().dependOnClassesThat()
                .resideInAnyPackage(BASE + ".domain..", BASE + ".service..", BASE + ".api..", BASE + ".repository..")
            .because("platform.. holds vendored cross-cutting infrastructure (error handling, audit, filters, "
                + "export, utils) that must stay reusable across services — it must never depend on this "
                + "app's own admin domain code, only the other direction is allowed");
        rule.check(classes);
    }
}
