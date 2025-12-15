package pt.rafap.kpb.core.gradle.content

data class Lib(
    val name: String,
    val versionRef: String,
    val id: String,
    val isTest: Boolean = false,
)