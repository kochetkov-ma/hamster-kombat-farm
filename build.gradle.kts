import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.24"
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTargetValidationMode.set(JvmTargetValidationMode.WARNING)
        jvmTarget = "17"
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("com.codeborne:selenide-appium:7.3.3") {
        exclude(module = "selenium-chrome-driver")
        exclude(module = "selenium-edge-driver")
        exclude(module = "selenium-firefox-driver")
        exclude(module = "selenium-ie-driver")
        exclude(module = "selenium-safari-driver")
        exclude(module = "selenium-manager")
        exclude(module = "selenium-devtools-v124")
        exclude(module = "selenium-devtools-v125")
        exclude(module = "selenium-devtools-v126")
        exclude(module = "selenium-devtools-v85")
        exclude(group = "io.opentelemetry")
        exclude(group = "io.opentelemetry.semconv")
    }
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("ch.qos.logback:logback-classic:1.5.6")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.17.1")
}

application {
    mainClass.set("org/brewcode/hamster/MainKt")
}

tasks.jar {
    from({
        configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.named<Sync>("installDist") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    include("hamster.*", "hamster", "brew-hamster.yaml", "upgrade.json")
}

tasks.named<CreateStartScripts>("startScripts") {
    doFirst {
        copy {
            from(rootDir)
            into("$outputDir")
            include("brew-hamster.yaml", "upgrade.json")
        }
    }
}
