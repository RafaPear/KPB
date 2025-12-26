package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.templates.Template

/**
 * DSL scope for building a [Project].
 *
 * Allows configuring project properties like group ID, adding modules, files,
 * Gradle files, and applying templates.
 */
class ProjectBuilderScope(val name: String) : BuilderScope {
    private val kpbFiles = mutableListOf<KpbFile>()
    private var group: String? = null
    private val modules = mutableListOf<Module>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()
    private var templates = mutableListOf<Template>()

    /**
     * Adds a file to the project root.
     */
    override fun fileProject(path: String, content: () -> String?) {
        kpbFiles.add(KpbFile(path, content()))
    }

    /**
     * Sets the project group ID.
     * Validates that the group ID format is correct (no spaces, valid dots, etc.).
     */
    override fun group(group: String) {
        if (group.isBlank()) this.group = null
        assert(!group.contains(" ")) { "Group cannot contain spaces." }
        assert(!group.endsWith(".")) { "Group cannot end with a dot." }
        assert(!group.startsWith(".")) { "Group cannot start with a dot." }
        assert(!group.contains("..")) { "Group cannot contain consecutive dots." }
        assert(!group.contains("/")) { "Group cannot contain slashes." }
        this.group = group
    }

    /**
     * Adds a pre-built [GradleFile] to the project.
     */
    override fun gradleFileProject(gradleFile: GradleFile) {
        versionCatalog += gradleFile.versionCatalog
        gradleFiles.add(gradleFile)
    }

    /**
     * Configures and adds a [GradleFile] using the DSL.
     */
    override fun gradleFileProject(
        name: String,
        gradleFile: GradleFileBuildScope.() -> Unit
    ) {
        val gradleFileScope = GradleFileBuildScope(name)
        gradleFileScope.gradleFile()
        val gradleFile = gradleFileScope.buildGradleFile()
        gradleFileProject(gradleFile)
    }

    /**
     * Adds a pre-built [Module] to the project.
     */
    override fun moduleProject(module: Module) {
        versionCatalog += module.versionCatalog
        modules.add(module)
    }

    /**
     * Configures and adds a [Module] using the DSL.
     */
    override fun moduleProject(
        name: String,
        simpleName: String,
        module: ModuleBuildScope.() -> Unit
    ): Module {
        val newGroup = "$group.$simpleName".trimEnd('.')
        val scope = ModuleBuildScope(name, simpleName, newGroup)
        scope.module()
        val module = scope.build()
        moduleProject(module)
        return module
    }

    /**
     * Removes a module from the project by its name.
     */
    override fun removeModule(moduleName: String) {
        modules.removeIf { it.name == moduleName }
    }

    /**
     * Applies a template to the project.
     * The template function receives the current project state (without parsing) to allow
     * conditional logic based on project configuration.
     */
    override fun templateProject(template: (Project) -> Template) {
        println("Applying template to project $name")
        templates.add(template(buildNoParse()))
    }

    private fun buildNoParse(): Project {
        return Project(
            name = name,
            group = group,
            versionCatalog = versionCatalog,
            modules = modules,
            kpbFiles = kpbFiles,
            gradleFiles = gradleFiles,
            templates = templates
        )
    }

    override fun buildProject(): Project {
        return buildNoParse().parseProject()
    }
}
