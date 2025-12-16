package pt.rafap.kpb.core.gradle.content

/**
 * Represents a version entry in the version catalog.
 *
 * @property name The alias name for the version.
 * @property version The version string value.
 */
data class Version(
    val name: String,
    val version: String
)