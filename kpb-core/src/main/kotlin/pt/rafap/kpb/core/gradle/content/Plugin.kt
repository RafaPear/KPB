package pt.rafap.kpb.core.gradle.content

/**
 * Represents a plugin defined in the version catalog.
 *
 * @property name The alias name for the plugin in the catalog.
 * @property id The plugin ID.
 * @property versionRef The reference to the version alias in the catalog.
 * @property apply Whether to apply the plugin immediately in the build file.
 */
data class Plugin(
    val name: String,
    val id: String,
    val versionRef: String,
    val apply: Boolean = true,
)