# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

RUN chmod +x ./gradlew
RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon dependencies

COPY src ./src

RUN --mount=type=cache,target=/root/.gradle ./gradlew --no-daemon bootJar
RUN set -eux; \
    jar_file="$(find build/libs -maxdepth 1 -type f -name '*.jar' ! -name '*-plain.jar' | head -n 1)"; \
    test -n "$jar_file"; \
    cp "$jar_file" /workspace/app.jar

FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

RUN addgroup -S taskchrono && adduser -S taskchrono -G taskchrono

ENV JAVA_OPTS="" \
    SPRING_DOCKER_COMPOSE_ENABLED=false

COPY --from=build /workspace/app.jar /app/app.jar

RUN chown -R taskchrono:taskchrono /app

USER taskchrono

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
