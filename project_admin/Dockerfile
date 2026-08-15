# Stage 1: Build — runs the full multi-module reactor, only the presentation
# module produces a runnable jar (it repackages its own artifact + all its
# module dependencies into a single fat jar).
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Layer Maven cache before copying source so dependency downloads are cached
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY common/pom.xml common/pom.xml
COPY persistence/pom.xml persistence/pom.xml
COPY business/pom.xml business/pom.xml
COPY presentation/pom.xml presentation/pom.xml
COPY test/pom.xml test/pom.xml
RUN ./mvnw dependency:go-offline --no-transfer-progress -q

COPY common/src common/src
COPY persistence/src persistence/src
COPY business/src business/src
COPY presentation/src presentation/src
COPY test/src test/src
RUN ./mvnw package -DskipTests -Dmaven.test.skip=true --no-transfer-progress -q -pl presentation -am

# Stage 2: Runtime
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Non-root user for security
RUN addgroup --system --gid 1001 app && \
    adduser  --system --uid 1001 --gid 1001 app && \
    mkdir -p /app/logs && \
    chown -R app:app /app

COPY --from=build --chown=app:app /workspace/presentation/target/admin-api.jar app.jar

USER app

EXPOSE 8080

# ZGC: low-pause GC suited for latency-sensitive services
# MaxRAMPercentage: container-aware heap sizing (replaces -Xmx)
# egd: avoids /dev/random blocking on some Linux distros
ENTRYPOINT ["java", \
  "-XX:+UseZGC", \
  "-XX:+ZGenerational", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
  "-jar", "app.jar"]
