# ─── Stage 1: Build ───────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copia solo pom.xml per sfruttare la cache dei layer delle dipendenze
COPY pom.xml ./

# Scarica le dipendenze in cache (eseguito solo se pom.xml cambia)
RUN mvn dependency:go-offline -Pproduction -q

# Copia il sorgente e build con profilo production
COPY src ./src
COPY frontend ./frontend

RUN mvn clean package -Pproduction -DskipTests

# ─── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre

WORKDIR /app

# Utente non-root per sicurezza
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser

COPY --from=builder /build/target/my-app-0.0.1-SNAPSHOT.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Xms256m", \
    "-Xmx512m", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
