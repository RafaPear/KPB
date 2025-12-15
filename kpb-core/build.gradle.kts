dependencies {
    implementation(project(":kpb-storage"))
    implementation(project(":kpb-utils"))
    testImplementation(kotlin("test"))
    implementation(libs.coroutines)
}