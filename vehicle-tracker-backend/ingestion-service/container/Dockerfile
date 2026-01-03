FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY ingestion-service /app/ingestion-service
COPY events /app/events
COPY schema /app/schema

RUN ./gradlew bootJar --no-daemon

RUN cp /app/ingestion-service/build/libs/ingestion-service-*.jar /app/app.jar
RUN rm -rf ingestion-service events schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]