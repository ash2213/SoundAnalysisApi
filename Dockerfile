FROM openjdk:17-jdk-slim

# 🔧 Installér nødvendige pakker for font-rendering (JFreeChart kræver dette)
RUN apt-get update && \
    apt-get install -y \
    fontconfig \
    libfreetype6 \
    fonts-dejavu-core \
    fonts-dejavu-extra && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# 🎯 Miljøvariabler
ARG DB_NAME
ARG DB_USERNAME
ARG DB_PASSWORD
ARG CONNECTION_STR
ARG SECRET_KEY
ARG ISSUER
ARG TOKEN_EXPIRE_TIME
ARG DEPLOYED

ENV DB_NAME=$DB_NAME \
    DB_USERNAME=$DB_USERNAME \
    DB_PASSWORD=$DB_PASSWORD \
    CONNECTION_STR=$CONNECTION_STR \
    SECRET_KEY=$SECRET_KEY \
    ISSUER=$ISSUER \
    TOKEN_EXPIRE_TIME=$TOKEN_EXPIRE_TIME \
    DEPLOYED=$DEPLOYED

# 📁 App setup
WORKDIR /app
COPY target/app.jar app.jar

EXPOSE 7000
ENTRYPOINT ["java", "-jar", "app.jar"]
