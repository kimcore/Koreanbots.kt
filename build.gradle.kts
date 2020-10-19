plugins {
    kotlin("jvm") version "1.4.10"
}

group = "com.github.kimcore.koreanbots"
version = "1.0"

repositories {
    mavenCentral()
    jcenter()
    maven("https://dl.bintray.com/kordlib/Kord")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.8")

    // JDA
    implementation ("net.dv8tion", "JDA", "4.2.0_209") {
        exclude(module = "opus-java")
    }

    // Kord
    implementation("com.gitlab.kordlib.kord", "kord-core", "0.6.3")

    // Javacord
    implementation("org.javacord", "javacord", "3.1.1")

    // Discord4J
    implementation("com.discord4j", "discord4j-core", "3.1.1")
}

tasks {
    compileKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        kotlinOptions {
            jvmTarget ="1.8"
        }
    }
    compileTestKotlin {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        kotlinOptions {
            jvmTarget ="1.8"
        }
    }
}