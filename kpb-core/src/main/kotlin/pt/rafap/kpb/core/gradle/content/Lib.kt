package pt.rafap.kpb.core.gradle.content

/**
 * Represents a library dependency defined in the version catalog.
 *
 * @property name The alias name for the library in the catalog.
 * @property versionRef The reference to the version alias in the catalog.
 * @property id The maven coordinates of the library (group:artifact).
 * @property isTest Whether this library is a test dependency.
 */
data class Lib(
    val name: String,
    val id: String,
    val versionRef: String,
    val write: Boolean = true,
    val isTest: Boolean = false,
)