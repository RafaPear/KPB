package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.*
import pt.rafap.kpb.core.mergeModules
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.KpbFile

/**
 * Represents a Gradle build file (e.g., build.gradle.kts or settings.gradle.kts).
 *
 * Contains configuration for plugins, dependencies, imports, and other build script elements.
 * It also holds a reference to the [VersionCatalog] entries required by this file.
 */
data class GradleFile(
    val name: String,
    val imports: List<String>,
    val plugins: List<Plugin>,
    val otherPlugins: List<OtherPlugin>,
    val dependencies: List<Dependency>,
    val libs: List<Lib>,
    val modules: List<Module>,
    val others: List<Other>,
    val repositories: List<String>,
    val versionCatalog: VersionCatalog
) {
    init {
        versionCatalog.verifyGradleFile(this)
    }

    /**
     * Converts this GradleFile object into a [KpbFile] ready to be written to disk.
     *
     * Generates the content string for the Gradle file, including imports, plugins block,
     * dependencies block, and other custom content.
     */
    fun toKbpFile(): KpbFile {
        val content = buildString {
            imports.forEach {
                appendLine("import $it")
            }
            appendLine()
            if (plugins.isNotEmpty() || otherPlugins.isNotEmpty()) {
                appendLine("plugins {")
                otherPlugins.forEach {
                    val applyStr = if (it.apply) "" else " apply false"
                    appendLine("    ${it.definition}$applyStr")
                }
                plugins.forEach {
                    val applyStr = if (it.apply) "" else " apply false"
                    val alias = it.name.replace("-", ".")
                    appendLine("    alias(libs.plugins.$alias)$applyStr")
                }
                appendLine("}")
                appendLine()
            }
            if (repositories.isNotEmpty()) {
                appendLine("repositories {")
                repositories.forEach {
                    appendLine("    $it")
                }
                appendLine("}")
                appendLine()
            }
            if (dependencies.isNotEmpty() || libs.isNotEmpty() || modules.isNotEmpty()) {
                appendLine("dependencies {")
                modules.forEach {
                    appendLine("    implementation(project(\":${it.name}\"))")
                }
                libs.forEach {
                    val configuration = if (it.isTest) "testImplementation" else "implementation"
                    if (it.write)
                        appendLine("    $configuration(libs.${it.name})")
                }
                dependencies.forEach {
                    appendLine("    ${it.definition}")
                }
                appendLine("}")
            }
            others.forEach {
                appendLine(it.content)
            }
        }.trimEnd()

        return KpbFile(
            path = name,
            content = content,
        )
    }

    /**
     * Merges this GradleFile with another GradleFile.
     *
     * Combines lists of imports, plugins, dependencies, etc., and merges the associated
     * VersionCatalogs.
     */
    operator fun plus(other: GradleFile): GradleFile {
        return GradleFile(
            name = this.name,
            imports = (this.imports + other.imports).distinct(),
            plugins = (this.plugins + other.plugins).distinctBy { it.id },
            otherPlugins = (this.otherPlugins + other.otherPlugins).distinct(),
            dependencies = (this.dependencies + other.dependencies).distinct(),
            libs = (this.libs + other.libs).distinctBy { it.name },
            modules = this.modules.mergeModules(other.modules),
            others = (this.others + other.others).distinct(),
            repositories = (this.repositories + other.repositories).distinct(),
            versionCatalog = this.versionCatalog + other.versionCatalog
        )
    }

    companion object {
        /**
         * Helper function to build a GradleFile using the [GradleFileBuildScope] DSL.
         */
        fun buildGradleFile(name: String, func: GradleFileBuildScope.() -> Unit): GradleFile {
            val scope = GradleFileBuildScope(name)
            scope.func()
            return scope.buildGradleFile()
        }
    }
}