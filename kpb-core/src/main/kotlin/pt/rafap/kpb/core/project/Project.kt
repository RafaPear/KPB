package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFile.Companion.buildGradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.gradle.content.Other
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.templates.Template
import pt.rafap.kpb.core.templates.Template.Companion.EmptyTemplate
import java.io.File

data class Project(
    val name: String,
    val group: String?,
    val versionCatalog: VersionCatalog,
    val modules: List<Module>,
    val gradleFiles: List<GradleFile>,
    val kbpFiles: List<KbpFile>,
    val templates: List<Template> = emptyList()
) {
    fun printStructure(prefix: String = "") {
        println("$prefix- Project: $name")
        gradleFiles.forEach { gradleFile ->
            println("$prefix  - Gradle File: ${gradleFile.name}")
        }
        kbpFiles.forEach { kbpFile ->
            println("$prefix  - Kbp File: ${kbpFile.path}")
        }
        modules.forEach { module ->
            println("$prefix  - Module: ${module.name}")
            module.files.forEach { kbpFile ->
                println("$prefix    - Kbp File: ${kbpFile.path}")
            }
        }
    }

    fun parseProject(): Project {
        var combinedProject = this.copy()
        val finalTemplate = templates.reduce { a, b -> a + b }
        combinedProject += finalTemplate

        val modules = combinedProject.modules.groupBy { it.name }
            .map { (_, sameNameFiles) ->
                sameNameFiles.reduce { acc, file -> acc + file }
            }


        val gradleFiles = combinedProject.gradleFiles.groupBy { it.name }
            .map { (_, sameNameFiles) ->
                sameNameFiles.reduce { acc, file -> acc + file }
            }


        combinedProject = combinedProject.copy(modules = modules, gradleFiles = gradleFiles)


        for (handler in finalTemplate.handlers) combinedProject = handler(combinedProject)

        return combinedProject
    }

    fun createProject() {
        val projectDir = File(name)
        if (!projectDir.exists()) {
            projectDir.mkdirs()
        }

        val kmpFiles: MutableList<KbpFile> = mutableListOf()

        // Create GradleFiles
        gradleFiles.forEach { kmpFiles += it.toKbpFile() }

        // Create KbpFiles
        kbpFiles.forEach { it.create() }

        // Create Modules
        modules.forEach { module ->
            val moduleDir = File(projectDir, module.name)
            if (!moduleDir.exists()) {
                moduleDir.mkdirs()
            }
            module.files.forEach {
                kmpFiles += KbpFile(
                    path = "${module.name}/${it.path}",
                    content = it.content
                )
            }
        }

        // Create version catalog
        versionCatalog.toKbpFile().also { kmpFiles += it }

        // Write all files
        kmpFiles.forEach {
            KbpFile("$name/${it.path}", it.content).create()
        }
    }


    operator fun plus(other: Project): Project {
        println("${this.name} + ${other.name}")
        return Project(
            name = this.name,
            group = this.group ?: other.group,
            versionCatalog = this.versionCatalog + other.versionCatalog,
            modules = this.modules + other.modules,
            kbpFiles = this.kbpFiles + other.kbpFiles,
            gradleFiles = this.gradleFiles + other.gradleFiles,
            templates = this.templates + other.templates
        )
    }

    operator fun plus(template: Template): Project {
        println("${this.name} + Template")
        return Project(
            name = this.name,
            group = this.group,
            versionCatalog = this.versionCatalog + template.versionCatalog,
            modules = this.modules + template.modules,
            kbpFiles = this.kbpFiles + template.kbpFiles,
            gradleFiles = this.gradleFiles + template.gradleFiles,
            templates = this.templates + template
        )
    }

    companion object {
        fun buildProject(name: String, func: ProjectBuilderScope.() -> Unit): Project {
            val scope = ProjectBuilderScope(name)
            scope.func()
            return scope.build()
        }
    }
}