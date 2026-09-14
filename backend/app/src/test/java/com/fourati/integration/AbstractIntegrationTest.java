package com.fourati.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;

/**
 * Base class for every integration test that needs the full application
 * context against real infrastructure: Postgres for data and Redis for the
 * shared stores (rate limit, idempotency, cache), exactly as the
 * multi-replica profiles run it ({@code fourati.common.store=redis} in
 * application-test.yml).
 *
 * One set of containers, one context. Containers are static singletons
 * started once per JVM and shared by every subclass; because all subclasses
 * declare the same test configuration, Spring's context cache reuses a single
 * application context across them as well.
 *
 * {@code @ServiceConnection} wires the datasource and the Redis connection
 * from the containers, so no {@code @DynamicPropertySource} boilerplate is
 * needed in subclasses.
 *
 * {@link MockMvc} is configured with the real security filter chain
 * (spring-security-test on the classpath applies it automatically), so HTTP
 * level tests exercise authentication, method security and the error contract
 * exactly as a client sees them.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    static {
        POSTGRES.start();
        REDIS.start();
    }

    @Autowired
    protected MockMvc mvc;
}
