package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project.Companion.buildProject
import pt.rafap.kpb.core.templates.createAppTemplate
import pt.rafap.kpb.core.templates.createDefaultTemplate
import pt.rafap.kpb.core.templates.createDokkaTemplate

fun main() {
    val pfs = buildProject("isip3") {
        group("pt.isel")

        module("cli") {
            srcMainFile("cli/Main.kt") {
                """
            package ${groupPath}/cli

            fun main() {
                println("Hello, KPB!")
            }
            """.trimIndent()
            }

            buildGradle {
                library("ktflag", "com.github.RafaPear:KtFlag") {
                    Version("ktflag", "1.5.4")
                }
                dependency {
                    Dependency("implementation(kotlin(\"stdlib\"))")
                }
            }
        }

        module("core") {
            srcMainFile("core/Example.kt") {
                """
            package ${groupPath}/core

            fun example(): String {
                return "This is an example from core module."
            }
            """.trimIndent()
            }
        }

        file("README.md") {
            """
            # ISI Project

            made using KPB - Kotlin Project Builder
            [read more](https://github.com/RafaPear/KPB)
            
            """.trimIndent()
        }

        template {
            it.createDokkaTemplate() + it.createDefaultTemplate() + it.createAppTemplate(listOf("cli"))
        }
    }

    pfs.printStructure()

    pfs.createProject()
}