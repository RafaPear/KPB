package pt.rafap.kpb.core.gradle.content

/**
 * Represents a plugin definition that is not managed by the version catalog.
 *
 * @property definition The plugin definition string (e.g., `id("com.example.plugin") version "1.0"`).
 * @property apply Whether to apply the plugin immediately.
 */
data class OtherPlugin(
    val definition: String,
    val apply: Boolean = true,
)