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

    implementation(libs.spring.boot.starter)
    implementation(libs.spring.integration.mqtt)
    implementation(libs.paho.client.mqttv3)
    implementation(libs.protobuf)

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}