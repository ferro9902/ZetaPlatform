# =============================================================================
# Multi-service Dockerfile for ZetaPlatform Spring Boot / gRPC services
#
# Build with: docker build --build-arg SERVICE_DIR=<service-dir-name> -t <tag> .
# Example:    docker build --build-arg SERVICE_DIR=user-auth-service \
#                          -t ghcr.io/ferro9902/user-auth-service:latest .
# =============================================================================

# --- Build stage -------------------------------------------------------------
FROM eclipse-temurin:25-jdk-noble AS build

ARG SERVICE_DIR
WORKDIR /workspace

# Copy the target service directory (contains pom.xml, src/, .mvn/, mvnw)
COPY ZetaPlatform/${SERVICE_DIR}/ .

# Make the Maven wrapper executable and run the build
RUN chmod +x mvnw && \
    ./mvnw package -DskipTests --no-transfer-progress

# --- Runtime stage -----------------------------------------------------------
FROM eclipse-temurin:25-jre-noble

WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar

# gRPC server and HTTP actuator/REST ports
EXPOSE 9090 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
