plugins {
    kotlin("jvm") version "1.6.20"
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":aula-04-05"))
    implementation(project(":aula-04-12"))

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // Use sl4j logger
    testImplementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("io.github.microutils:kotlin-logging:1.12.5")
}