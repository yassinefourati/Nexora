package com.fourati.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
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
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Structural rules inside the modules (ADR 0004). Cross-module boundaries are
 * verified by {@link ModularityTest}; the rules here keep each module's
 * layers honest, keep the platform free of domain knowledge, and keep the
 * application layer organised by use case.
 *
 * Layout of every domain module:
 *   <module>/                 public API (records, facade, events)
 *   <module>/internal/domain       JPA entities, enums, value objects
 *   <module>/internal/application  use cases, services, mappers, listeners, jobs
 *   <module>/internal/persistence  repositories, read-model queries
 *   <module>/internal/web          controllers
 */
class ArchitectureTest {

    private static final String BASE = "com.fourati";
    private static final String[] DOMAIN_MODULES = {
        "identity", "access", "organization", "configuration", "content", "notification", "audit"
    };
    private static final String SERVICE = "org.springframework.stereotype.Service";

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(BASE);
    }

    @Test
    void controllers_must_not_use_repositories_or_mappers() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..internal.web..", BASE + ".app.web..")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("Mapper")
            .because("controllers are thin adapters: they bind the request, call one use case and map the result");
        rule.check(classes);
    }

    @Test
    void controllers_must_be_annotated_with_RestController() {
        ArchRule rule = classes()
            .that().resideInAnyPackage("..internal.web..", BASE + ".app.web..")
            .and().haveSimpleNameEndingWith("Controller")
            .should().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .because("all controllers must be Spring REST controllers");
        rule.check(classes);
    }

    @Test
    void services_must_be_annotated_with_Service() {
        ArchRule rule = classes()
            .that().resideInAPackage("..internal.application..")
            .and().haveSimpleNameEndingWith("Service")
            .should().beAnnotatedWith(SERVICE)
            .because("all service classes must be Spring-managed beans");
        rule.check(classes);
    }

    @Test
    void use_cases_expose_exactly_one_public_method() {
        ArchRule rule = classes()
            .that().resideInAPackage("..internal.application..")
            .and().areAnnotatedWith(SERVICE)
            .and().haveSimpleNameNotEndingWith("Service")
            .should(haveExactlyOnePublicMethod())
            .because("a use case is one business operation: one verb-named class, one execute method, "
                + "owning the transaction, the authorization and the audit of that operation (ADR 0004)");
        rule.check(classes);
    }

    @Test
    void mutating_use_cases_declare_the_permission_they_need() {
        ArchRule rule = classes()
            .that().resideInAPackage("..internal.application..")
            .and().areAnnotatedWith(SERVICE)
            .and().haveSimpleNameNotEndingWith("Service")
            .and().haveSimpleNameNotEndingWith("Query")
            .and().haveSimpleNameNotStartingWith("Find")
            .and().haveSimpleNameNotStartingWith("Search")
            .should(guardTheirPublicMethodWithPreAuthorize())
            .because("authorization travels with the operation (ADR 0006): a mutating use case names the "
                + "permission it needs, whether it is called from HTTP, a job or another module");
        rule.check(classes);
    }

    @Test
    void status_fields_in_the_domain_are_enums_not_strings() {
        ArchRule rule = fields()
            .that().haveName("status")
            .and().areDeclaredInClassesThat().resideInAPackage("..internal.domain..")
            .should().notHaveRawType(String.class)
            .because("a lifecycle is an enum with behaviour on the aggregate, not a string compared to literals");
        rule.check(classes);
    }

    @Test
    void domain_classes_must_not_have_spring_stereotypes() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..internal.domain..")
            .should().beAnnotatedWith("org.springframework.stereotype.Component")
            .orShould().beAnnotatedWith(SERVICE)
            .orShould().beAnnotatedWith("org.springframework.stereotype.Repository")
            .orShould().beAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .because("domain objects are plain Java — free of framework coupling");
        rule.check(classes);
    }

    @Test
    void domain_and_persistence_must_not_know_the_web_or_security_stack() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..internal.domain..", "..internal.persistence..")
            .should().dependOnClassesThat()
                .resideInAnyPackage("jakarta.servlet..", "org.springframework.web..", "org.springframework.security..")
            .because("entities and repositories are reachable from jobs, listeners and tests that have no request");
        rule.check(classes);
    }

    @Test
    void platform_must_not_depend_on_any_application_module() {
        ArchRule rule = noClasses()
            .that().resideInAPackage(BASE + ".platform..")
            .should().dependOnClassesThat().resideInAnyPackage(
                BASE + ".identity..", BASE + ".access..", BASE + ".organization..", BASE + ".configuration..",
                BASE + ".content..", BASE + ".notification..", BASE + ".audit..", BASE + ".app..", BASE + ".shared..")
            .because("platform.. is a reusable library: it must never depend on this application's own code");
        rule.check(classes);
    }

    @Test
    void repositories_are_only_used_inside_their_own_module() {
        for (String module : DOMAIN_MODULES) {
            ArchRule rule = noClasses()
                .that().resideOutsideOfPackage(BASE + "." + module + "..")
                .should().dependOnClassesThat().resideInAPackage(BASE + "." + module + ".internal.persistence..")
                .because("another module asks '" + module + "' through its facade or events, never through its repositories");
            rule.check(classes);
        }
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
            .that().areDeclaredInClassesThat().haveSimpleNameEndingWith("Controller")
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

    private static ArchCondition<JavaClass> haveExactlyOnePublicMethod() {
        return new ArchCondition<>("expose exactly one public method") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                long publicMethods = javaClass.getMethods().stream()
                    .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                    .filter(m -> !m.getModifiers().contains(JavaModifier.SYNTHETIC))
                    .filter(m -> !m.getModifiers().contains(JavaModifier.STATIC))
                    .count();
                if (publicMethods != 1) {
                    events.add(SimpleConditionEvent.violated(javaClass,
                        javaClass.getName() + " exposes " + publicMethods + " public methods; a use case exposes one"));
                }
            }
        };
    }

    private static ArchCondition<JavaClass> guardTheirPublicMethodWithPreAuthorize() {
        String preAuthorize = "org.springframework.security.access.prepost.PreAuthorize";
        return new ArchCondition<>("guard every public method with @PreAuthorize") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                javaClass.getMethods().stream()
                    .filter(m -> m.getModifiers().contains(JavaModifier.PUBLIC))
                    .filter(m -> !m.getModifiers().contains(JavaModifier.SYNTHETIC))
                    .filter(m -> !m.getModifiers().contains(JavaModifier.STATIC))
                    .filter(m -> !m.isAnnotatedWith(preAuthorize))
                    .forEach(m -> events.add(SimpleConditionEvent.violated(javaClass,
                        m.getFullName() + " is a mutating use case with no @PreAuthorize")));
            }
        };
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
}
