package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a template for testing configuration.
 *
 * Adds JUnit 5 and Mockk dependencies to the specified modules and configures
 * the test task to use the JUnit Platform.
 *
 * @param modules The list of modules to configure with testing support.
 * @return A Template configured with testing dependencies and tasks.
 */
fun Project.createTestTemplate(modules: List<Module>): Template {
    return this.buildTemplate {
        modules.map { module ->
            moduleTemplate(module.name, module.simpleName) {
                buildGradleModule {
                    dependency { Dependency("testImplementation(kotlin(\"test\"))") }
                    library("mockk", "io.mockk:mockk", isTest = true) {
                        Version("mockk", "1.13.9")
                    }
                    other {
                        """
                        tasks.withType<Test> {
                            useJUnitPlatform()
                        }
                        """.trimIndent()
                    }
                }
            }
        }
    }
}
