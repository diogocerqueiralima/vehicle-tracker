plugins {
    id("java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.github.diogocerqueiralima"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    implementation(project(":schema"))
    implementation(project(":error-common"))
    implementation(project(":api-common"))
    implementation(libs.spring.boot.grpc)
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.postgresql)
    implementation(libs.protobuf.java.util)
    implementation(libs.springdoc)
    implementation(platform(libs.spring.grpc.dependencies))

    testImplementation(libs.spring.boot.starter.test)
}

tasks.test {
    useJUnitPlatform()
}