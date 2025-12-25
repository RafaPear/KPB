package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.project.KpbFile
import pt.rafap.kpb.core.project.Project

/**
 * DSL scope for building a template.
 *
 * Provides functions to define template content including files, modules, Gradle files,
 * and transformation handlers.
 *
 * @property rootProject The project that this template is being built for.
 */
class TemplateBuilderScope(val rootProject: Project): BuilderScope {
    private val kpbFiles = mutableListOf<KpbFile>()
    private val modules = mutableListOf<Module>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()
    private var handlers = mutableListOf<(Project) -> Project>()

    override fun fileTemplate(path: String, content: () -> String?) {
        kpbFiles.add(KpbFile(path, content()))
    }

    override fun gradleFileTemplate(gradleFile: GradleFile) {
        versionCatalog += gradleFile.versionCatalog
        gradleFiles.add(gradleFile)
    }

    override fun gradleFileTemplate(
        name: String,
        gradleFile: GradleFileBuildScope.() -> Unit
    ) {
        val gradleFileScope = GradleFileBuildScope(name)
        gradleFileScope.gradleFile()
        val gradleFile = gradleFileScope.buildGradleFile()
        gradleFileTemplate(gradleFile)
    }

    override fun moduleTemplate(module: Module) {
        versionCatalog += module.versionCatalog
        modules.add(module)
    }

    override fun handler(handler: (Project) -> Project) {
        handlers.add(handler)
    }

    override fun moduleTemplate(
        name: String,
        simpleName: String,
        module: ModuleBuildScope.() -> Unit
    ) {
        val group = if (rootProject.group == null) simpleName
        else if (simpleName.isBlank()) rootProject.group
        else "${rootProject.group}.$simpleName"

        val moduleScope = ModuleBuildScope(name, simpleName, group)
        moduleScope.module()
        val module = moduleScope.build()
        moduleTemplate(module)
    }

    override fun buildTemplate(): Template {
        return Template(
            versionCatalog = versionCatalog,
            modules = modules,
            gradleFiles = gradleFiles,
            kpbFiles = kpbFiles,
            handlers = handlers
        )
    }
}
