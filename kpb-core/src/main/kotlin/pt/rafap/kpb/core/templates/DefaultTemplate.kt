package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile.Companion.buildGradleFile
import pt.rafap.kpb.core.gradle.content.Other
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

fun Project.createDefaultTemplate(): Template = this.buildTemplate {
    val moduleNames = modules.map { it.name }
    gradleFile(
        name = "settings.gradle.kts"
    ) {
        other {
            """
                    rootProject.name = "${it.name}"

                    enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

                    plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }
                """.trimIndent()
        }
        other {
            moduleNames.joinToString(separator = "\n") { "include(\":$it\")" }
        }
    }

    gradleFile(
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