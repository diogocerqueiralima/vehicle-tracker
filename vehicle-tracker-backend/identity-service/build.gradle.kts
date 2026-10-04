plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.allopen)
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
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.grpc)
    implementation(libs.bouncy.castle.provider)
    implementation(libs.bouncy.castle.bcpkix)
    implementation(libs.postgresql)
    implementation(libs.springdoc)
    implementation(libs.kotlin.reflect)
    implementation(platform(libs.spring.grpc.dependencies))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// JPA entities must stay non-final so Hibernate can proxy them
allOpen {
    annotation("jakarta.persistence.Entity")
}

tasks.test {
    useJUnitPlatform()
}