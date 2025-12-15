package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.KbpFile
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.project.ProjectBuilderScope

data class Template(
    val versionCatalog: VersionCatalog,
    val modules: List<Module>,
    val gradleFiles: List<GradleFile>,
    val kbpFiles: List<KbpFile>,
    val handlers: List<(Project) -> Project>,
) {

    operator fun plus(other: Template): Template {
        return Template(
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules + other.modules,
            gradleFiles = gradleFiles + other.gradleFiles,
            kbpFiles = this.kbpFiles + other.kbpFiles,
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

        val EmptyTemplate = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kbpFiles = emptyList(),
            handlers = emptyList()
        )
    }
}