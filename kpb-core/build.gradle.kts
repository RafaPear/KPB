plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(project(":kpb-utils"))
    testImplementation(kotlin("test"))
    implementation(libs.coroutines)
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.2")
}