package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.mergeGradleFiles
import pt.rafap.kpb.core.project.KpbFile

data class Module(
    val name: String,
    val files: List<KpbFile>,
    val gradleFiles: List<GradleFile>,
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
        val newGradleFiles = gradleFiles.mergeGradleFiles(other.gradleFiles)
        val combinedVersionCatalog = this.versionCatalog + other.versionCatalog
        val module = Module(
            name = this.name,
            files = combinedFiles,
            gradleFiles = newGradleFiles,
            versionCatalog = combinedVersionCatalog
        )
        return module
    }

    companion object {
        fun buildModule(name: String, group: String? = null, func: ModuleBuildScope.() -> Unit): Module {
            val newGroup = group?.replace(".", "/")
            val scope = ModuleBuildScope(name, newGroup)
            scope.func()
            return scope.build()
        }
    }
}