plugins {
    kotlin("jvm") version "1.6.21"
    java
}

version = "unspecified"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))

    implementation("junit:junit:4.13.1")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // Use sl4j logger
    testImplementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("org.slf4j:slf4j-simple:1.8.0-beta4")
    implementation("io.github.microutils:kotlin-logging:1.12.5")
    // internal dependencies
    implementation(project(":aula-05-05-async-intro"))

    // swing coroutines scope
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.5.2")


    implementation("org.http4k:http4k-core:4.11.0.1")
    implementation("org.http4k:http4k-server-jetty:4.11.0.1")
    implementation("org.http4k:http4k-client-okhttp:4.11.0.1")
    implementation("org.http4k:http4k-client-apache:4.11.0.1")

    implementation("org.jclarion:image4j:0.7")
}

