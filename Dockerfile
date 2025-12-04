# Multi-stage build Dockerfile for Spring Boot application
# Stage 1: Build stage
FROM maven:3.9-eclipse-temurin-21-jammy AS builder

WORKDIR /build

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Copy source code
COPY src src/
COPY .mvn .mvn/
COPY mvnw .
COPY mvnw.cmd .

# Build the application
RUN mvn -B -DskipTests clean package

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Copy the built JAR from the builder stage
COPY --from=builder /build/target/*.jar app.jar

# Expose port 8080
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/ml/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
