import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10" apply false
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
    }

    group = "com.github.kimcore.koreanbots"
    version = "1.0"

    repositories {
        mavenCentral()
        jcenter()
    }

    val implementation by configurations

    dependencies {
        // Kotlin
        implementation(kotlin("stdlib"))
        implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.8")
    }

    tasks.withType(KotlinCompile::class) {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}