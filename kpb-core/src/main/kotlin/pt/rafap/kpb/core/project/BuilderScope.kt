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
    fun fileProject(path: String, content: () -> String? = { null })
    fun group(group: String)
    fun gradleFileProject(name: String, gradleFile: GradleFileBuildScope.() -> Unit)
    fun gradleFileProject(gradleFile: GradleFile)
    fun moduleProject(name: String, simpleName: String, module: ModuleBuildScope.() -> Unit): Module
    fun moduleProject(module: Module)
    fun templateProject(template: (Project) -> Template)
    fun buildProject(): Project
}
