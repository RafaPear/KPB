package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.mergeGradleFiles
import pt.rafap.kpb.core.mergeModules
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.templates.Template
import pt.rafap.kpb.utils.LogManager
import java.io.File

private val logger = LogManager.getLogger("Project")

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
        group?.let {
            println("$prefix  Group: $it")
        }
        println("$prefix  Version Catalog:")

        versionCatalog.libs.forEach {
            println("$prefix    Lib: ${it.name} -> ${it.id} : ${it.versionRef}")
        }

        versionCatalog.versions.forEach {
            println("$prefix    Version: ${it.name} -> ${it.version}")
        }

        versionCatalog.plugins.forEach {
            println("$prefix    Plugin: ${it.id} -> ${it.versionRef}")
        }

        println("$prefix  Gradle Files:")

        gradleFiles.forEach {
            println("$prefix    - ${it.name}")
        }

        println("$prefix  Modules:")

        modules.forEach { module ->
            println("$prefix    - Module: ${module.name}")
            module.printStructure("$prefix      ")
        }

        println("$prefix  KPB Files:")

        kpbFiles.forEach {
            println("$prefix    - ${it.path}")
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

        if (templates.isNotEmpty()) {
            val finalTemplate = templates.reduce { a, b -> a + b }
            combinedProject += finalTemplate

            for (handler in finalTemplate.handlers)
                combinedProject += handler(combinedProject)
        }
        var versionCatalog = combinedProject.versionCatalog
        combinedProject.gradleFiles.forEach { gradleFile ->
            versionCatalog += gradleFile.versionCatalog
        }

        combinedProject.modules.map { module ->
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
    fun createProject(path: String = "") {
        val nPath =
            if (path.isBlank() || path.isEmpty()) name
            else if (path.endsWith("/")) path.dropLast(1) + name
            else "$path/$name"
        logger.info("Creating project structure for '${name}'")
        val projectDir = File(nPath)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val kpbFiles: MutableList<KpbFile> = this.kpbFiles.toMutableList()

        // Create GradleFiles
        gradleFiles.forEach { kpbFiles += it.toKbpFile() }

        // Create Modules
        modules.forEach { module ->
            val moduleLogger = LogManager.getLogger("Module.${module.name}")
            moduleLogger.info("Processing module '${module.name}'")
            val moduleDir = File(projectDir, module.name)
            if (!moduleDir.exists()) {
                moduleDir.mkdirs()
            }
            module.files.forEach {
                kpbFiles += KpbFile(
                    path = "${module.name}/${it.path}",
                    content = it.content
                )
                moduleLogger.info("  Added file: ${it.path}")
            }
            module.gradleFiles.forEach {
                kpbFiles += KpbFile(
                    path = "${module.name}/${it.name}",
                    content = it.toKbpFile().content
                )
                moduleLogger.info("  Added gradle file: ${it.name}")
            }
        }

        // Create version catalog
        versionCatalog.toKbpFile().also { kpbFiles += it }

        // Write all files
        kpbFiles.forEach {
            KpbFile("$nPath/${it.path}", it.content).create()
            logger.info("Created file: ${it.path}")
        }
        logger.info("Project creation complete.")
    }


    /**
     * Merges this project with another project configuration.
     *
     * Combines version catalogs, modules, files, and templates.
     * The group ID is taken from this project if present, otherwise from the other project.
     */
    operator fun plus(other: Project): Project {
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

    operator fun plus(other: KpbFile): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog,
            modules = this.modules,
            kpbFiles = (this.kpbFiles + other).distinct(),
            gradleFiles = this.gradleFiles,
            templates = this.templates
        )
    }

    operator fun plus(other: Module): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules.mergeModules(listOf(other)),
            kpbFiles = this.kpbFiles,
            gradleFiles = this.gradleFiles,
            templates = this.templates
        )
    }

    operator fun plus(other: VersionCatalog): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog + other,
            modules = this.modules,
            kpbFiles = this.kpbFiles,
            gradleFiles = this.gradleFiles,
            templates = this.templates
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

    operator fun plus(other: GradleFile): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules,
            kpbFiles = (this.kpbFiles + other.toKbpFile()).distinct(),
            gradleFiles = this.gradleFiles.mergeGradleFiles(listOf(other)),
            templates = this.templates
        )
    }

    operator fun plus(other: List<Any>): Project {
        var result = this
        for (item in other) {
            when (item) {
                is Module -> result += item
                is KpbFile -> result += item
                is VersionCatalog -> result += item
                is Template -> result += item
                is GradleFile -> result += item
                else -> throw IllegalArgumentException("Unsupported type in project addition: ${item::class}")
            }
        }
        return result
    }

    operator fun minus(other: Project): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog - other.versionCatalog,
            modules = this.modules.filterNot { mod -> other.modules.any { it.name == mod.name } },
            kpbFiles = this.kpbFiles.filterNot { file -> other.kpbFiles.any { it.path == file.path } },
            gradleFiles = this.gradleFiles.filterNot { gf -> other.gradleFiles.any { it.name == gf.name } },
            templates = this.templates.filterNot { tpl -> other.templates.any { it == tpl } }
        )
    }

    operator fun minus(other: Module): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog - other.versionCatalog,
            modules = this.modules.filterNot { it.name == other.name },
            kpbFiles = this.kpbFiles,
            gradleFiles = this.gradleFiles,
            templates = this.templates
        )
    }

    operator fun minus(other: VersionCatalog): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog - other,
            modules = this.modules,
            kpbFiles = this.kpbFiles,
            gradleFiles = this.gradleFiles,
            templates = this.templates
        )
    }

    operator fun minus(other: KpbFile): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog,
            modules = this.modules,
            kpbFiles = this.kpbFiles.filterNot { it.path == other.path },
            gradleFiles = this.gradleFiles,
            templates = this.templates
        )
    }

    operator fun minus(other: GradleFile): Project {
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog - other.versionCatalog,
            modules = this.modules,
            kpbFiles = this.kpbFiles.filterNot { it.path == other.toKbpFile().path },
            gradleFiles = this.gradleFiles.filterNot { it.name == other.name },
            templates = this.templates
        )
    }

    operator fun minus(other: List<Any>): Project {
        var result = this
        for (item in other) {
            when (item) {
                is Module -> result -= item
                is KpbFile -> result -= item
                is VersionCatalog -> result -= item
                is GradleFile -> result -= item
                else -> throw IllegalArgumentException("Unsupported type in project subtraction: ${item::class}")
            }
        }
        return result
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