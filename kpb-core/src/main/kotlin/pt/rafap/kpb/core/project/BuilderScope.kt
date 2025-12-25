package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.templates.Template

/**
 * DSL scope interface for building a project.
 *
 * Defines the available operations for configuring a project, such as adding files,
 * setting the group ID, adding Gradle files, modules, and applying templates.
 */
interface BuilderScope {
    /**
     * Adds a file to the project root.
     *
     * @param path The relative path of the file.
     * @param content A lambda that returns the file content as a string.
     */
    fun fileProject(path: String, content: () -> String? = { null })

    /**
     * Sets the project's group ID.
     *
     * @param group The group identifier (e.g., "com.example").
     */
    fun group(group: String)

    /**
     * Adds a Gradle file using the DSL.
     *
     * @param name The name of the Gradle file (e.g., "build.gradle.kts").
     * @param gradleFile The configuration block.
     */
    fun gradleFileProject(name: String, gradleFile: GradleFileBuildScope.() -> Unit)

    /**
     * Adds a pre-built Gradle file to the project.
     *
     * @param gradleFile The Gradle file to add.
     */
    fun gradleFileProject(gradleFile: GradleFile)

    /**
     * Creates and adds a module to the project using the DSL.
     *
     * @param name The full module name.
     * @param simpleName The simple name of the module.
     * @param module The configuration block.
     * @return The created Module.
     */
    fun moduleProject(name: String, simpleName: String, module: ModuleBuildScope.() -> Unit): Module

    /**
     * Adds a pre-built module to the project.
     *
     * @param module The module to add.
     */
    fun moduleProject(module: Module)

    /**
     * Applies a template to the project.
     *
     * @param template A function that creates a template from the current project.
     */
    fun templateProject(template: (Project) -> Template)

    /**
     * Builds and returns the configured project.
     *
     * @return The built Project instance.
     */
    fun buildProject(): Project
}
