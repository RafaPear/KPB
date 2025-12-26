package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a template for Kotlin Coroutines support.
 *
 * Adds the kotlinx-coroutines-core dependency to the specified modules.
 *
 * @param modules The list of modules to configure with Coroutines support.
 * @return A Template configured with Coroutines dependencies.
 */
fun Project.createCoroutinesTemplate(modules: List<Module>): Template {
    return this.buildTemplate {
        modules.map { module ->
            moduleTemplate(module.name, module.simpleName) {
                buildGradleModule {
                    library("coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core") {
                        Version("coroutines", "1.10.2")
                    }
                }
            }
        }
    }
}

