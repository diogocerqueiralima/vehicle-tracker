FROM eclipse-temurin:21-jdk

RUN apt-get update
RUN apt-get install -y libtpm2-pkcs11-tools libtpm2-pkcs11-1

WORKDIR /app

COPY gradle /app/gradle
COPY settings.gradle.kts /app/settings.gradle.kts
COPY gradlew /app/gradlew
COPY identity-service /app/identity-service
COPY schema /app/schema
COPY tpm/pkcs11.cfg /app/pkcs11.cfg

RUN ./gradlew bootJar --no-daemon

RUN cp /app/identity-service/build/libs/identity-service-*.jar /app/app.jar
RUN rm -rf identity-service schema gradle gradlew settings.gradle.kts

CMD ["java", "-jar", "/app/app.jar"]