FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY processing-service /app/processing-service
COPY events /app/events
COPY schema /app/schema

RUN ./gradlew bootJar --no-daemon

RUN cp /app/processing-service/build/libs/processing-service-*.jar /app/app.jar
RUN rm -rf processing-service events schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]