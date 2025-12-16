package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.mergeGradleFiles
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.KpbFile
import pt.rafap.kpb.core.project.Project

data class Template(
    val versionCatalog: VersionCatalog,
    val modules: List<Module>,
    val gradleFiles: List<GradleFile>,
    val kpbFiles: List<KpbFile>,
    val handlers: List<(Project) -> Project>,
) {

    operator fun plus(other: Template): Template {
        return Template(
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules + other.modules,
            gradleFiles = gradleFiles.mergeGradleFiles(other.gradleFiles),
            kpbFiles = this.kpbFiles + other.kpbFiles,
            handlers = this.handlers + other.handlers,
        )
    }

    companion object {
        fun Project.buildTemplate(scope: TemplateBuilderScope.(Project) -> Unit): Template {
            val project = this
            val templateScope = TemplateBuilderScope(project)
            templateScope.scope(project)
            return templateScope.build()
        }
    }
}