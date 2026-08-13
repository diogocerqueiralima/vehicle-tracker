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

    implementation(project(":api-common"))
    implementation(libs.spring.boot.starter)
    implementation(libs.spring.boot.starter.webflux)
    implementation(libs.spring.boot.starter.oauth2.resource.server)
    implementation(libs.spring.boot.starter.data.redis.reactive)
    implementation(libs.spring.boot.starter.validation)

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.mockito:mockito-core")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
