package pt.rafap.kpb.core.gradle.content

data class Plugin(
    val name: String,
    val id: String,
    val versionRef: String,
    val apply: Boolean = true,
)