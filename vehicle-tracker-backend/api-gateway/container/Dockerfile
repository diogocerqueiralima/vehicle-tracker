FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY api-gateway /app/api-gateway
COPY api-common /app/api-common
COPY error-common /app/error-common

RUN mkdir -p ingestion-service events schema identity-service asset-service

RUN ./gradlew :api-gateway:bootJar --no-daemon

RUN cp /app/api-gateway/build/libs/api-gateway-1.0.0.jar /app/app.jar
RUN rm -rf api-gateway api-common error-common gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]
