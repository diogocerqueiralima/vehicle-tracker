FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY gateway-service /app/gateway-service
COPY asset-service /app/asset-service
COPY ingestion-service /app/ingestion-service
COPY identity-service /app/identity-service
COPY events /app/events
COPY error-common /app/error-common
COPY api-common /app/api-common
COPY schema /app/schema

RUN ./gradlew :gateway-service:bootJar --no-daemon

RUN cp /app/gateway-service/build/libs/gateway-service-*.jar /app/app.jar
RUN rm -rf gateway-service asset-service ingestion-service identity-service events error-common api-common schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]
