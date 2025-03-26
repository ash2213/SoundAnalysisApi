# 🏗️ Build stage
FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# 🚀 Final image
FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY --from=build /app/target/SoundAnalysisApi-1.0-SNAPSHOT-shaded.jar app.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]

