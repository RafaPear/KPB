package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.templates.Template

class ProjectBuilderScope(val name: String): BuilderScope {
    private val kbpFiles = mutableListOf<KbpFile>()
    private var group: String? = null
    private val modules = mutableListOf<Module>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()
    private var templates = mutableListOf<Template>()

    override fun file(path: String, content: () -> String?) {
        kbpFiles.add(KbpFile("$name/$path", content()))
    }

    override fun group(group: String) {
        if (group.isBlank()) this.group = null
        assert(!group.contains(" ")) { "Group cannot contain spaces." }
        assert(!group.endsWith(".")) { "Group cannot end with a dot." }
        assert(!group.startsWith(".")) { "Group cannot start with a dot." }
        assert(!group.contains("..")) { "Group cannot contain consecutive dots." }
        assert(!group.contains("/")) { "Group cannot contain slashes." }
        this.group = group.replace(".", "/")
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

    override fun module(
        name: String,
        module: ModuleBuildScope.() -> Unit
    ) {
        val moduleScope = ModuleBuildScope(name, group)
        moduleScope.module()
        val module = moduleScope.build()
        module(module)
    }

    override fun template(template: (Project) -> Template) {
        println("Applying template to project $name")
        templates.add(template(buildNoParse()))
    }

    private fun buildNoParse(): Project {
        return Project(
            name = name,
            group = group,
            versionCatalog = versionCatalog,
            modules = modules,
            kbpFiles = kbpFiles,
            gradleFiles = gradleFiles,
            templates = templates
        )
    }

    override fun build(): Project {
        return buildNoParse().parseProject()
    }
}
