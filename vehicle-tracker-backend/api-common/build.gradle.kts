plugins {
    id("java")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
}

group = "com.github.diogocerqueiralima.api.common"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {

    compileOnly(project(":error-common"))
    compileOnly(libs.spring.boot.starter)
    compileOnly(libs.spring.boot.starter.web)
    compileOnly(libs.spring.boot.starter.validation)
    compileOnly(libs.springdoc)

}