package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Lib
import pt.rafap.kpb.core.gradle.content.Plugin
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.project.KbpFile

data class VersionCatalog(
    val libs: List<Lib> = listOf(),
    val versions: List<Version> = listOf(),
    val plugins: List<Plugin> = listOf()
) {
    init {
        verifyIntegrity()
    }

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

    fun toKbpFile(): KbpFile {
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
        return KbpFile(
            path = "gradle/libs.versions.toml",
            content = sb.toString()
        )
    }

    operator fun plus(other: VersionCatalog): VersionCatalog {
        val libs = (this.libs + other.libs).distinct()
        val versions = (this.versions + other.versions).distinct()
        val plugins = (this.plugins + other.plugins).distinct()

        println("Merging VersionCatalogs:")
        println(" - Libs: $libs")
        println(" - Versions: $versions")
        println(" - Plugins: $plugins")

        return VersionCatalog(
            libs = libs,
            versions = versions,
            plugins = plugins
        )
    }

    operator fun minus(other: VersionCatalog): VersionCatalog {
        return VersionCatalog(
            libs = this.libs.filterNot { lib -> other.libs.any { it.name == lib.name } },
            versions = this.versions.filterNot { ver -> other.versions.any { it.name == ver.name } },
            plugins = this.plugins.filterNot { plugin -> other.plugins.any { it.id == plugin.id } }
        )
    }
}