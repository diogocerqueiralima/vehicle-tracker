FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY vehicle-service /app/vehicle-service
COPY schema /app/schema

RUN ./gradlew bootJar --no-daemon

RUN cp /app/vehicle-service/build/libs/vehicle-service-*.jar /app/app.jar
RUN rm -rf vehicle-service schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]