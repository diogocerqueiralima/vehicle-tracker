FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY asset-service /app/asset-service
COPY schema /app/schema
COPY error-common /app/error-common
COPY api-common /app/api-common

RUN mkdir -p ingestion-service identity-service events api-gateway

RUN ./gradlew :asset-service:bootJar --no-daemon

RUN cp /app/asset-service/build/libs/asset-service-1.0.0.jar /app/app.jar
RUN rm -rf asset-service schema error-common api-common gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]
