import com.apollographql.apollo.gradle.internal.ApolloDownloadSchemaTask

plugins {
    id("com.apollographql.apollo") version "2.4.1"
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx", "kotlinx-coroutines-jdk8", "1.3.8")

    // GraphQL
    implementation("com.apollographql.apollo", "apollo-runtime", "2.4.1")
    implementation("com.apollographql.apollo", "apollo-coroutines-support", "2.4.1")

    // HTTP
    implementation("com.github.kittinunf.fuel", "fuel", "2.2.3")
    implementation("com.github.kittinunf.fuel", "fuel-coroutines", "2.2.3")

    // Logging
    implementation("org.slf4j", "slf4j-api", "1.7.30")

    // Util
    implementation("com.google.code.gson", "gson", "2.8.6")
}


val downloadSchema = tasks.register("downloadSchema", ApolloDownloadSchemaTask::class.java) {
    endpoint.set("https://api.beta.koreanbots.dev/v2/graphql/endpoint")
    schemaRelativeToProject.set("src/main/graphql/com/github/kimcore/koreanbots/schema.json")
}

tasks {
    compileKotlin {
        dependsOn(downloadSchema)
    }
}