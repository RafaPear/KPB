package pt.rafap.kpb.core.project

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.templates.Template

interface BuilderScope {
    fun file(path: String, content: () -> String? = { null })
    fun group(group: String)
    fun gradleFile(name: String, gradleFile: GradleFileBuildScope.() -> Unit)
    fun gradleFile(gradleFile: GradleFile)
    fun module(name: String, module: ModuleBuildScope.() -> Unit)
    fun module(module: Module)
    fun template(template: (Project) -> Template)
    fun build(): Project
}
