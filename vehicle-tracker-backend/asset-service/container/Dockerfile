FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY asset-service /app/asset-service
COPY ingestion-service /app/ingestion-service
COPY identity-service /app/identity-service
COPY events /app/events
COPY error-common /app/error-common
COPY api-common /app/api-common
COPY schema /app/schema

RUN ./gradlew :asset-service:bootJar --no-daemon

RUN cp /app/asset-service/build/libs/asset-service-*.jar /app/app.jar
RUN rm -rf asset-service ingestion-service identity-service events schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]
