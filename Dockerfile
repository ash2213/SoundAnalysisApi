# Use Eclipse Temurin as base image for Java 17
FROM eclipse-temurin:17-jdk as build

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Package the application (skip tests for speed, optional)
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# --- Final stage ---
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 7000

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
