package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.module.Module
import kotlin.collections.component1
import kotlin.collections.component2

fun List<Module>.mergeModules(other: List<Module>): List<Module> =
    (this + other).groupBy { it.name }.map { (_, sameNameFiles) ->
        sameNameFiles.reduce { acc, file -> acc + file }
    }

fun List<GradleFile>.mergeGradleFiles(other: List<GradleFile>): List<GradleFile> =
    (this + other).groupBy { it.name }.map { (_, sameNameFiles) ->
        sameNameFiles.reduce { acc, file -> acc + file }
    }