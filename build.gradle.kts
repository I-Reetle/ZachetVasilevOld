plugins {
    kotlin("jvm") version "2.0.20"
    id("ru.yarsu.json-project-properties")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

group = "ru.ac.uniyar"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

ktlint {
    version.set("1.3.1")
}
