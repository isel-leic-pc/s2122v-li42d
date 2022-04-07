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
}