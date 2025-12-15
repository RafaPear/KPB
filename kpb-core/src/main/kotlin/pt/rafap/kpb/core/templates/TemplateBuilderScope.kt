package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.project.KbpFile
import pt.rafap.kpb.core.project.Project

class TemplateBuilderScope(val rootProject: Project): BuilderScope {
    private val kbpFiles = mutableListOf<KbpFile>()
    private val modules = mutableListOf<Module>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()
    private var handlers = mutableListOf<(Project) -> Project>()

    override fun file(path: String, content: () -> String?) {
        kbpFiles.add(KbpFile(path, content()))
    }

    override fun gradleFile(gradleFile: GradleFile) {
        versionCatalog += gradleFile.versionCatalog
        gradleFiles.add(gradleFile)
    }

    override fun gradleFile(
        name: String,
        gradleFile: GradleFileBuildScope.() -> Unit
    ) {
        val gradleFileScope = GradleFileBuildScope(name)
        gradleFileScope.gradleFile()
        val gradleFile = gradleFileScope.build()
        gradleFile(gradleFile)
    }

    override fun module(module: Module) {
        versionCatalog += module.versionCatalog
        modules.add(module)
    }

    override fun handler(handler: (Project) -> Project) {
        handlers.add(handler)
    }

    override fun module(
        name: String,
        module: ModuleBuildScope.() -> Unit
    ) {
        val moduleScope = ModuleBuildScope(name, rootProject.group)
        moduleScope.module()
        val module = moduleScope.build()
        module(module)
    }

    override fun build(): Template {
        return Template(
            versionCatalog = versionCatalog,
            modules = modules,
            gradleFiles = gradleFiles,
            kbpFiles = kbpFiles,
            handlers = handlers
        )
    }
}
