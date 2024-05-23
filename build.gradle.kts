plugins {
    kotlin("jvm") version "1.9.23"
    application
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("io.appium:java-client:9.2.2")
    implementation("com.codeborne:selenide:7.3.2")
    implementation("org.seleniumhq.selenium:selenium-java:4.21.0")
    implementation("io.github.oshai:kotlin-logging-jvm:6.0.9")
    implementation("org.slf4j:slf4j-simple:2.0.13")
}

application {
    mainClass.set("MainKt")
}
