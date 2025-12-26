package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a template for Kotlin Serialization support.
 *
 * Applies the serialization plugin and adds the JSON serialization library.
 *
 * @param modules The list of modules to configure with Serialization support.
 * @return A Template configured with Serialization plugin and dependencies.
 */
fun Project.createSerializationTemplate(modules: List<Module>): Template {
    return this.buildTemplate {
        modules.map { module ->
            moduleTemplate(module.name, module.simpleName) {
                buildGradleModule {
                    plugin("serialization", "org.jetbrains.kotlin.plugin.serialization") {
                        Version("kotlin", "2.2.20")
                    }
                    library("serializationJson", "org.jetbrains.kotlinx:kotlinx-serialization-json") {
                        Version("serialization", "1.8.0")
                    }
                }
            }
        }
    }
}

