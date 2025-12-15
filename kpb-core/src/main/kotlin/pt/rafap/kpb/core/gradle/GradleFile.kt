package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.project.KbpFile
import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Lib
import pt.rafap.kpb.core.gradle.content.Other
import pt.rafap.kpb.core.gradle.content.OtherPlugin
import pt.rafap.kpb.core.gradle.content.Plugin
import pt.rafap.kpb.core.module.Module

data class GradleFile(
    val name: String,
    val imports: List<String>,
    val plugins: List<Plugin>,
    val otherPlugins: List<OtherPlugin>,
    val dependencies: List<Dependency>,
    val libs: List<Lib>,
    val modules: List<Module>,
    val others: List<Other>,
    val versionCatalog: VersionCatalog
) {
    init {
        versionCatalog.verifyGradleFile(this)
    }

    fun toKbpFile(): KbpFile {
        val content = buildString {
            imports.forEach {
                appendLine("import $it")
            }
            if (plugins.isNotEmpty() || otherPlugins.isNotEmpty()) {
                appendLine("plugins {")
                otherPlugins.forEach {
                    val applyStr = if (it.apply) "" else " apply false"
                    appendLine("    ${it.definition}$applyStr")
                }
                plugins.forEach {
                    val applyStr = if (it.apply) "" else " apply false"
                    appendLine("    alias(libs.plugins.${it.name})$applyStr")
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

        return KbpFile(
            path = name,
            content = content,
        )
    }

    operator fun plus(other: GradleFile): GradleFile {
        return GradleFile(
            name = this.name,
            imports = this.imports + other.imports,
            plugins = this.plugins + other.plugins,
            otherPlugins = this.otherPlugins + other.otherPlugins,
            dependencies = this.dependencies + other.dependencies,
            libs = this.libs + other.libs,
            modules = this.modules + other.modules,
            others = this.others + other.others,
            versionCatalog = this.versionCatalog + other.versionCatalog
        )
    }

    companion object {
        fun buildGradleFile(name: String, func: GradleFileBuildScope.() -> Unit): GradleFile {
            val scope = GradleFileBuildScope(name)
            scope.func()
            return scope.build()
        }
    }
}