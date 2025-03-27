# Bruger Java 17 som base
FROM openjdk:17-jdk-slim

# Angiv JAR filen (shaded)
ARG JAR_FILE=target/*-shaded.jar

# Build args (modtages fra GitHub Actions)
ARG DB_NAME
ARG DB_USERNAME
ARG DB_PASSWORD
ARG CONNECTION_STR
ARG SECRET_KEY
ARG ISSUER
ARG TOKEN_EXPIRE_TIME
ARG DEPLOYED

# Eksponer som environment variabler
ENV DB_NAME=$DB_NAME \
    DB_USERNAME=$DB_USERNAME \
    DB_PASSWORD=$DB_PASSWORD \
    CONNECTION_STR=$CONNECTION_STR \
    SECRET_KEY=$SECRET_KEY \
    ISSUER=$ISSUER \
    TOKEN_EXPIRE_TIME=$TOKEN_EXPIRE_TIME \
    DEPLOYED=$DEPLOYED

# Arbejdsmappe og kopiér JAR
WORKDIR /app
COPY ${JAR_FILE} app.jar

EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]
