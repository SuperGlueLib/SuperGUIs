plugins {
    kotlin("jvm") version "1.9.0"
    `maven-publish`
}

group = "com.github.supergluelib"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://www.jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.github.SuperGlueLib:SuperFoundations:8ec859478a")
}

kotlin {
    jvmToolchain(17)
}

publishing.publications.create<MavenPublication>("maven") {
    from(components["java"])
}