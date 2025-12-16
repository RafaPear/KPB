package pt.rafap.kpb.core.gradle.content

/**
 * Represents a raw dependency string in a Gradle file.
 *
 * @property definition The full dependency declaration string (e.g., "implementation(\"com.example:lib:1.0\")").
 */
data class Dependency(
    val definition: String,
)
