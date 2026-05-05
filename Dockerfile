# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-25-alpine AS builder

WORKDIR /app

# Cachear dependencias antes de copiar el código fuente
COPY pom.xml ./
RUN mvn dependency:go-offline -q

COPY src/ src/
RUN mvn package

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:25-jdk-alpine AS runtime

# UTF-8 a nivel JVM (C.UTF-8 es soportado nativamente por musl/Alpine)
ENV LANG=C.UTF-8 \
    LC_ALL=C.UTF-8 \
    JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Duser.timezone=America/Santiago"

# Usuario no-root
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

COPY --from=builder /app/target/AgendarReservas-0.0.1-SNAPSHOT.jar app.jar

RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
