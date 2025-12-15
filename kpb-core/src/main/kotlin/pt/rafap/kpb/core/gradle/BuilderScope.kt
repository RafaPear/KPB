package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module

interface BuilderScope {
    fun plugin (name: String, id: String, apply: Boolean, version: () -> Version)
    fun otherPlugin(definition: String, apply: Boolean)
    fun dependency(dependency: () -> Dependency)
    fun library(name: String, id: String, version: () -> Version)
    fun module(module: () -> Module)
    fun other  (content: () -> String)
    fun import (path: String)
    fun build(): GradleFile
}
