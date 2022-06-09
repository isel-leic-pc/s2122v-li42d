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
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}