plugins {
    kotlin("jvm") version "2.1.0"
    application
}
kotlin { jvmToolchain(17) }
dependencies {
    implementation("com.github.TeamNewPipe:NewPipeExtractor:v0.27.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
application { mainClass.set("MainKt") }
