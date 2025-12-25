package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Lib
import pt.rafap.kpb.core.gradle.content.Plugin
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.KpbFile

/**
 * Represents a Gradle Version Catalog (libs.versions.toml).
 *
 * Holds collections of libraries, versions, and plugins to be defined in the catalog.
 * Ensures integrity by checking for duplicates.
 */
data class VersionCatalog(
    val libs: List<Lib> = listOf(),
    val versions: List<Version> = listOf(),
    val plugins: List<Plugin> = listOf()
) {
    init {
        verifyIntegrity()
    }

    /**
     * Verifies that there are no duplicate IDs or names in the catalog entries.
     * Throws IllegalArgumentException if duplicates are found.
     */
    fun verifyIntegrity() {
        require(plugins.map { it.id }.toSet().size == plugins.size) {
            "Duplicate plugin IDs found in VersionCatalog: " +
                    plugins.groupBy { it.id }
                        .filter { it.value.size > 1 }
                        .keys.joinToString(", ")
        }
        require(libs.map { it.name }.toSet().size == libs.size) {
            "Duplicate library names found in VersionCatalog: " +
                    libs.groupBy { it.name }
                        .filter { it.value.size > 1 }
                        .keys.joinToString(", ")
        }
        require(versions.map { it.name }.toSet().size == versions.size) {
            "Duplicate version names found in VersionCatalog: " +
                    versions.groupBy { it.name }
                        .filter { it.value.size > 1 }
                        .keys.joinToString(", ")
        }
    }

    /**
     * Verifies that a given [GradleFile] only uses plugins and libraries defined in this catalog.
     */
    fun verifyGradleFile(gradleFile: GradleFile) {
        val plugins = gradleFile.plugins
        val libs = gradleFile.libs

        require(plugins.all { plugin -> this.plugins.any { it.id == plugin.id && it.versionRef == plugin.versionRef } }) {
            "All plugins in GradleFile must be defined in VersionCatalog."
        }
        require(libs.all { lib -> this.libs.any { it.name == lib.name && it.versionRef == lib.versionRef && it.id == lib.id } }) {
            "All libraries in GradleFile must be defined in VersionCatalog."
        }
    }

    /**
     * Generates the `libs.versions.toml` file content from this catalog.
     */
    fun toKbpFile(): KpbFile {
        val sb = StringBuilder()
        sb.appendLine("[versions]")
        versions.forEach { version ->
            sb.appendLine("${version.name} = \"${version.version}\"")
        }
        sb.appendLine()
        sb.appendLine("[libraries]")
        libs.forEach { lib ->
            sb.appendLine("${lib.name} = { module = \"${lib.id}\", version.ref = \"${lib.versionRef}\" }")
        }
        sb.appendLine()
        sb.appendLine("[plugins]")
        plugins.forEach { plugin ->
            sb.appendLine("${plugin.name} = { id = \"${plugin.id}\", version.ref = \"${plugin.versionRef}\" }")
        }
        return KpbFile(
            path = "gradle/libs.versions.toml",
            content = sb.toString()
        )
    }

    /**
     * Merges this catalog with another, combining entries and removing duplicates.
     */
    operator fun plus(other: VersionCatalog): VersionCatalog {
        val libs = (this.libs + other.libs).distinctBy { it.name }
        val versions = (this.versions + other.versions).distinctBy { it.name }
        val plugins = (this.plugins + other.plugins).distinctBy { it.id }

        return VersionCatalog(
            libs = libs,
            versions = versions,
            plugins = plugins
        )
    }

    /**
     * Removes entries present in the other catalog from this one.
     */
    operator fun minus(other: VersionCatalog): VersionCatalog {
        return VersionCatalog(
            libs = this.libs.filterNot { lib -> other.libs.any { it.name == lib.name } },
            versions = this.versions.filterNot { ver -> other.versions.any { it.name == ver.name } },
            plugins = this.plugins.filterNot { plugin -> other.plugins.any { it.id == plugin.id } }
        )
    }
}