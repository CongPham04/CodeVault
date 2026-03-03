# ===================================================
# Stage 1: Build the application
# ===================================================
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and POM first (for dependency caching)
COPY mvnw mvnw
COPY .mvn .mvn
COPY pom.xml pom.xml

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached unless pom.xml changes)
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src src

# Build the JAR (skip tests for faster builds)
RUN ./mvnw package -DskipTests -B

# ===================================================
# Stage 2: Run the application
# ===================================================
FROM eclipse-temurin:21-jre

WORKDIR /app

# Create storage directory
RUN mkdir -p /var/codevault-storage/repos

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/repos || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
