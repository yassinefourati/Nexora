# admin-api image (ADR 0008, Phase 6 of docs/backend-architecture-plan.md)
#
#   build   : JDK 25 compiles the two-module reactor (bytecode level from pom.xml),
#             then explodes the fat jar into Docker layers (dependencies change
#             rarely, application code on every build) and pulls the SBOM out.
#   runtime : JRE 25, non-root, a CDS archive trained on this exact JVM,
#             collector and classpath so the app starts warm
#             (spring.context.exit=onRefresh runs the whole context once without
#             any infrastructure, profile `cds`), the CycloneDX SBOM next to the
#             jar and served by /actuator/sbom on the management port.
#
# Build args (all optional): GIT_SHA, BUILD_DATE, VERSION -> OCI labels.

# ---------------------------------------------------------------- build --
FROM eclipse-temurin:25-jdk AS build
WORKDIR /workspace

# Layer the Maven cache before the sources so dependency downloads are reused
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY platform/pom.xml platform/pom.xml
COPY app/pom.xml app/pom.xml
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw dependency:go-offline --no-transfer-progress -q

COPY platform/src platform/src
COPY app/src app/src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests -Dmaven.test.skip=true --no-transfer-progress -q -pl app -am

# Explode the fat jar into layers: dependencies / spring-boot-loader /
# snapshot-dependencies / application. application/ holds a slim admin-api.jar
# whose manifest points at lib/, which is what CDS wants (no nested-jar loader).
RUN java -Djarmode=tools -jar app/target/admin-api.jar extract --layers --destination /workspace/extracted \
 && mkdir -p /workspace/sbom \
 && cp app/target/classes/META-INF/sbom/application.cdx.json /workspace/sbom/admin-api.cdx.json

# -------------------------------------------------------------- runtime --
FROM eclipse-temurin:25-jre AS runtime
ARG GIT_SHA=unknown
ARG BUILD_DATE=unknown
ARG VERSION=1.0.0
WORKDIR /app

RUN addgroup --system --gid 1001 app \
 && adduser  --system --uid 1001 --gid 1001 app \
 && mkdir -p /app/logs /app/uploads \
 && chown -R app:app /app

# Rarely-changing layers first so a code change only invalidates the last one
COPY --from=build --chown=app:app /workspace/extracted/dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/spring-boot-loader/ ./
COPY --from=build --chown=app:app /workspace/extracted/snapshot-dependencies/ ./
COPY --from=build --chown=app:app /workspace/extracted/application/ ./
COPY --from=build --chown=app:app /workspace/sbom/ ./sbom/

# CDS training run: same JVM, same collector, same classpath and working directory
# as the ENTRYPOINT (an archive is only loadable under the GC it was dumped with).
# The `cds` profile needs no database, Redis, RabbitMQ or Keycloak.
RUN java -XX:+UseG1GC -XX:ArchiveClassesAtExit=/app/admin-api.jsa \
         -Dspring.profiles.active=cds -Dspring.context.exit=onRefresh \
         -jar admin-api.jar \
 && chown app:app /app/admin-api.jsa \
 && rm -rf /app/logs/*

LABEL org.opencontainers.image.title="admin-api" \
      org.opencontainers.image.description="ProjectAdmin backend: modular monolith on Spring Boot 4" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${GIT_SHA}" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.source="https://github.com/yassinefourati/project_fullstack" \
      io.fourati.sbom="/app/sbom/admin-api.cdx.json"

USER app
EXPOSE 8080 8081

# G1: CDS can only map archived heap objects with compressed oops, which ZGC disables. Measured on
#     this image, G1 starts warm in ~3.9 s (6.1 s cold) where ZGC manages ~4.9 s and logs an error
#     per start. Pauses at a 1.5 GB heap are milliseconds either way. To go back to ZGC, change this
#     flag and the training run above together.
# MaxRAMPercentage: container-aware heap sizing (replaces -Xmx)
# SharedArchiveFile: the CDS archive trained above; -Xshare:auto falls back silently if it cannot map
# egd: avoids /dev/random blocking on some Linux distros
ENTRYPOINT ["java", \
  "-XX:+UseG1GC", \
  "-XX:MaxRAMPercentage=75.0", \
  "-XX:SharedArchiveFile=/app/admin-api.jsa", \
  "-Xshare:auto", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
  "-jar", "admin-api.jar"]
