repositories {
    maven("https://dl.bintray.com/kordlib/Kord")
}

dependencies {
    implementation(project(":core"))

    // Kord
    implementation("com.gitlab.kordlib.kord", "kord-core", "0.6.3")
}
