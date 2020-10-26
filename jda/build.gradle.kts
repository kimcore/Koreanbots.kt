dependencies {
    implementation(project(":core"))

    // JDA
    implementation("net.dv8tion", "JDA", "4.2.0_209") {
        exclude(module = "opus-java")
    }
}
