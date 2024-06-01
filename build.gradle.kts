plugins {
    kotlin("jvm") version "1.9.23"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("io.appium:java-client:9.2.3")
    implementation("com.codeborne:selenide-appium:7.3.2")
    implementation("org.seleniumhq.selenium:selenium-java:4.21.0")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")

}

application {
    mainClass.set("MainKt")
}
