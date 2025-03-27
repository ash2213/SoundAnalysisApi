# Start med en Java-base image
FROM openjdk:17-jdk-slim

# Angiv hvor JAR-filen ligger
ARG JAR_FILE=target/*.jar

# Build arguments (modtages fra GitHub Actions)
ARG DB_NAME
ARG DB_USERNAME
ARG DB_PASSWORD
ARG CONNECTION_STR
ARG SECRET_KEY
ARG ISSUER
ARG TOKEN_EXPIRE_TIME
ARG DEPLOYED

# Konverter ARGs til ENV (så appen kan læse dem med System.getenv)
ENV DB_NAME=$DB_NAME \
    DB_USERNAME=$DB_USERNAME \
    DB_PASSWORD=$DB_PASSWORD \
    CONNECTION_STR=$CONNECTION_STR \
    SECRET_KEY=$SECRET_KEY \
    ISSUER=$ISSUER \
    TOKEN_EXPIRE_TIME=$TOKEN_EXPIRE_TIME \
    DEPLOYED=$DEPLOYED

# Opret app mappe og kopiér JAR-filen ind
WORKDIR /app
COPY ${JAR_FILE} app.jar

# Eksponér standardport
EXPOSE 7070

# Start app
ENTRYPOINT ["java", "-jar", "app.jar"]
