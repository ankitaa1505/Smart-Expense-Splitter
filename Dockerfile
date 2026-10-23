# ==============================================================================
# Multi-Stage Dockerfile for Smart Expense Tracker REST API
# Stage 1: Build the artifact using Maven and Temurin JDK 21
# Stage 2: Minimal, secure JRE 21 runtime running as non-root user
# ==============================================================================

# ----------------- Stage 1: Build -----------------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /build

# Install Maven
RUN apk add --no-cache maven

# Pre-cache Maven dependencies layer
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy application source code and compile
COPY src ./src
RUN mvn clean package -DskipTests -B

# Extract Spring Boot layered jar for optimized container caching
WORKDIR /build/target
RUN java -Djarmode=layertools -jar *.jar extract

# ----------------- Stage 2: Runtime -----------------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create a non-privileged system group and user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy extracted layers from builder stage
COPY --from=builder /build/target/dependencies/ ./
COPY --from=builder /build/target/spring-boot-loader/ ./
COPY --from=builder /build/target/snapshot-dependencies/ ./
COPY --from=builder /build/target/application/ ./

# Change ownership to non-root user
RUN chown -R spring:spring /app

USER spring:spring

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
