plugins {
    kotlin("jvm") version "1.9.0"
}

group = "me.superpenguin.superglue"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://www.jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.19.4-R0.1-SNAPSHOT")
    implementation("com.github.SuperGlueLib:SuperFoundations:1.2.0")
}

kotlin {
    jvmToolchain(17)
}