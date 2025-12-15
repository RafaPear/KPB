package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.project.KbpFile

data class Module(
    val name: String,
    val files: List<KbpFile>,
    val versionCatalog: VersionCatalog
) {

    operator fun plus(other: Module): Module {
        if (this.name != other.name) {
            throw IllegalArgumentException("Cannot combine modules with different names: '${this.name}' and '${other.name}'")
        }
        val combinedFiles = this.files.toMutableList()
        for (file in other.files) {
            if (combinedFiles.none { it.path == file.path }) {
                combinedFiles += file
            }
        }
        val combinedVersionCatalog = this.versionCatalog + other.versionCatalog
        return Module(
            name = this.name,
            files = combinedFiles,
            versionCatalog = combinedVersionCatalog
        )
    }

    companion object {
        fun buildModule(name: String, group: String? = null, func: ModuleBuildScope.() -> Unit): Module {
            val scope = ModuleBuildScope(name, group)
            scope.func()
            return scope.build()
        }
    }
}