package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.mergeModules
import pt.rafap.kpb.core.mergeGradleFiles
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.templates.Template
import java.io.File

/**
 * Represents a project configuration in the KPB system.
 *
 * A Project holds all the necessary information to generate a complete project structure on disk,
 * including modules, Gradle build files, version catalogs, and arbitrary files.
 *
 * @property name The name of the project.
 * @property group The group ID of the project (e.g., "com.example").
 * @property versionCatalog The version catalog containing library and plugin versions.
 * @property modules The list of modules contained in the project.
 * @property gradleFiles The list of Gradle build files (e.g., build.gradle.kts, settings.gradle.kts).
 * @property kpbFiles The list of arbitrary files to be created in the project root.
 * @property templates The list of templates applied to the project.
 */
data class Project(
    val name: String,
    val group: String?,
    val versionCatalog: VersionCatalog,
    val modules: List<Module>,
    val gradleFiles: List<GradleFile>,
    val kpbFiles: List<KpbFile>,
    val templates: List<Template> = emptyList()
) {
    /**
     * Prints the virtual structure of the project to the console.
     * Useful for debugging the project configuration before generation.
     *
     * @param prefix The prefix string for indentation (used for recursive printing).
     */
    fun printStructure(prefix: String = "") {
        println("$prefix- Project: $name")
        gradleFiles.forEach { gradleFile ->
            println("$prefix  - Gradle File: ${gradleFile.name}")
        }
        kpbFiles.forEach { kbpFile ->
            println("$prefix  - Kbp File: ${kbpFile.path}")
        }
        modules.forEach { module ->
            println("$prefix  - Module: ${module.name}")
            module.files.forEach { kbpFile ->
                println("$prefix    - Kbp File: ${kbpFile.path}")
            }
        }
    }

    /**
     * Processes the project configuration by applying templates and merging duplicate entries.
     *
     * This method:
     * 1. Combines all applied templates into a single configuration.
     * 2. Merges modules and Gradle files with the same name.
     * 3. Executes any template handlers to modify the project structure.
     *
     * @return A new [Project] instance with the fully resolved configuration.
     */
    fun parseProject(): Project {
        var combinedProject = this
        val finalTemplate = templates.reduce { a, b -> a + b }
        combinedProject += finalTemplate

        for (handler in finalTemplate.handlers)
            combinedProject += handler(combinedProject)

        var versionCatalog = combinedProject.versionCatalog
        combinedProject.gradleFiles.forEach { gradleFile ->
            versionCatalog += gradleFile.versionCatalog
        }

        combinedProject.modules.map{ module ->
            module.gradleFiles.forEach { gradleFile ->
                versionCatalog += gradleFile.versionCatalog
            }
            versionCatalog += module.versionCatalog
        }

        combinedProject = combinedProject.copy(versionCatalog = versionCatalog)

        return combinedProject
    }

    /**
     * Materializes the project structure on disk.
     *
     * Creates directories, writes files, and generates the version catalog file.
     * All files are written relative to the current working directory under a folder named [name].
     */
    fun createProject() {
        val projectDir = File(name)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val kpbFiles: MutableList<KpbFile> = this.kpbFiles.toMutableList()

        // Create GradleFiles
        gradleFiles.forEach { kpbFiles += it.toKbpFile() }

        // Create Modules
        modules.forEach { module ->
            val moduleDir = File(projectDir, module.name)
            if (!moduleDir.exists()) {
                moduleDir.mkdirs()
            }
            module.files.forEach {
                kpbFiles += KpbFile(
                    path = "${module.name}/${it.path}",
                    content = it.content
                )
            }
            module.gradleFiles.forEach {
                kpbFiles += KpbFile(
                    path = "${module.name}/${it.name}",
                    content = it.toKbpFile().content
                )
            }
        }

        // Create version catalog
        versionCatalog.toKbpFile().also { kpbFiles += it }

        // Write all files
        kpbFiles.forEach {
            KpbFile("$name/${it.path}", it.content).create()
        }
    }


    /**
     * Merges this project with another project configuration.
     *
     * Combines version catalogs, modules, files, and templates.
     * The group ID is taken from this project if present, otherwise from the other project.
     */
    operator fun plus(other: Project): Project {
        println("${this.name} + ${other.name}")
        return Project(
            name = this.name,
            group = this.group ?: other.group,
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules.mergeModules(other.modules),
            kpbFiles = (this.kpbFiles + other.kpbFiles).distinct(),
            gradleFiles = this.gradleFiles.mergeGradleFiles(other.gradleFiles),
            templates = this.templates + other.templates
        )
    }

    /**
     * Applies a template to this project.
     *
     * Merges the template's configuration (version catalog, modules, files) into the project.
     */
    operator fun plus(other: Template): Project {
        println("${this.name} + Template")
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules.mergeModules(other.modules),
            kpbFiles = (this.kpbFiles + other.kpbFiles).distinct(),
            gradleFiles = this.gradleFiles.mergeGradleFiles(other.gradleFiles),
            templates = this.templates + other
        )
    }

    companion object {
        /**
         * Helper function to build a [Project] using the [ProjectBuilderScope] DSL.
         *
         * @param name The name of the project.
         * @param func The configuration block.
         * @return The built [Project] instance.
         */
        fun buildProject(name: String, func: ProjectBuilderScope.() -> Unit): Project {
            val scope = ProjectBuilderScope(name)
            scope.func()
            return scope.buildProject()
        }
    }
}