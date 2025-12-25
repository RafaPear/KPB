package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a default Kotlin/JVM project template.
 *
 * Generates a basic project structure with settings.gradle.kts, build.gradle.kts,
 * gradle wrapper, and version catalog configuration.
 *
 * @param version The project version (must follow semantic versioning: MAJOR[.MINOR][.PATCH]).
 * @return A Template configured with default Kotlin project settings.
 * @throws IllegalArgumentException if the version string is not in the correct format.
 */
fun Project.createDefaultTemplate(version: String): Template = this.buildTemplate { project ->
    val isVersionValid =
        Regex("^[1-9]\\d*(?:\\.(0|[1-9]\\d*))?(?:\\.(0|[1-9]\\d*))?$")
            .matches(version)

    if (!isVersionValid) {
        throw IllegalArgumentException(
            "Illegal version: '$version' is not a valid version.\n" +
                    "Correct format: 'MAJOR[.MINOR][.PATCH]', where:\n" +
                    "MAJOR is an integer > 0;\n" +
                    "MINOR is an optional non-negative integer;\n" +
                    "PATCH is an optional non-negative integer."
        )
    }
    val moduleNames = modules.map { it.name }
    gradleFileTemplate(
        name = "settings.gradle.kts"
    ) {
        other {
            """
                    rootProject.name = "${project.name}"

                    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

                    plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
                """.trimIndent()
        }
        other {
            moduleNames.joinToString(separator = "\n") { "include(\":$it\")" }
        }
    }

    gradleFileTemplate(
        name = "build.gradle.kts"
    ) {
        plugin("kotlin", "org.jetbrains.kotlin.jvm", apply = false) {
            Version("kotlin", "2.2.20")
        }
        other {
            val sb = StringBuilder()
            if (group != null) {
                sb.appendLine("group = \"${group.replace("/", ".")}\"\n")
            }
            sb.appendLine(
                """
            version = "$version"
            
            allprojects {
                repositories {
                    gradlePluginPortal()
                    mavenCentral()
                }
                    
                apply(plugin = "org.jetbrains.kotlin.jvm")
            }
            """.trimIndent()
            )
            sb.toString()
        }
    }
}