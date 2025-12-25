package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.ModuleBuildScope
import pt.rafap.kpb.core.project.Project

interface BuilderScope {
    fun fileTemplate(path: String, content: () -> String? = { null })
    fun gradleFileTemplate(name: String, gradleFile: GradleFileBuildScope.() -> Unit)
    fun gradleFileTemplate(gradleFile: GradleFile)
    fun moduleTemplate(name: String, simpleName: String, module: ModuleBuildScope.() -> Unit)
    fun moduleTemplate(module: Module)
    fun handler(handler: (Project) -> Project)
    fun buildTemplate(): Template
}
