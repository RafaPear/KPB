package pt.rafap.kpb.core.gradle.content

/**
 * Represents an import statement in a Gradle file.
 *
 * @property path The fully qualified name of the class or package to import.
 */
@Suppress("unused")
data class Import(val path: String)
