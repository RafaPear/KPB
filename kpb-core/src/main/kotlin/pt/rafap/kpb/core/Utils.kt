package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.module.Module
import kotlin.collections.component1
import kotlin.collections.component2

/**
 * Merges a list of modules with another list, combining modules with the same name.
 *
 * Modules with identical names are merged using the [Module.plus] operator.
 *
 * @param other The list of modules to merge with this list.
 * @return A new list with merged modules.
 */
fun List<Module>.mergeModules(other: List<Module>): List<Module> =
    (this + other).groupBy { it.name }.map { (_, sameNameFiles) ->
        sameNameFiles.reduce { acc, file -> acc + file }
    }

/**
 * Merges a list of Gradle files with another list, combining files with the same name.
 *
 * Gradle files with identical names are merged using the [GradleFile.plus] operator.
 *
 * @param other The list of Gradle files to merge with this list.
 * @return A new list with merged Gradle files.
 */
fun List<GradleFile>.mergeGradleFiles(other: List<GradleFile>): List<GradleFile> =
    (this + other).groupBy { it.name }.map { (_, sameNameFiles) ->
        sameNameFiles.reduce { acc, file -> acc + file }
    }