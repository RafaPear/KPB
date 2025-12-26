package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a template for CLI applications using KtFlag.
 *
 * Adds the KtFlag dependency for command-line argument parsing.
 *
 * @param modules The list of modules to configure as CLI applications.
 * @return A Template configured with CLI dependencies.
 */
fun Project.createCliTemplate(modules: List<Module>): Template {
    val template = this.buildTemplate {
        modules.map { module ->
            moduleTemplate(module.name, module.simpleName) {
                buildGradleModule {
                    repository("maven { url = uri(\"https://jitpack.io\") }")
                    library("ktflag", "com.github.RafaPear:KtFlag") {
                        Version("ktflag", "1.5.4")
                    }
                }
            }
        }
    }
    return template + createAppTemplate(modules)
}
