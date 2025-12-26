package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module

interface BuilderScope {
    fun plugin(name: String, id: String, apply: Boolean = true, version: () -> Version)
    fun otherPlugin(definition: String, apply: Boolean = true)
    fun dependency(dependency: () -> Dependency)
    fun library(name: String, id: String, write: Boolean = true, isTest: Boolean = false, version: () -> Version)
    fun moduleGradle(module: () -> Module)
    fun moduleGradle(module: Module)
    fun other(content: () -> String)
    fun import(path: String)
    fun repository(repository: String)
    fun buildGradleFile(): GradleFile
}
