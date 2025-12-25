package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project.Companion.buildProject
import pt.rafap.kpb.core.templates.Template.Companion.addAllTemplates
import pt.rafap.kpb.core.templates.createAppTemplate
import pt.rafap.kpb.core.templates.createComposeTemplate
import pt.rafap.kpb.core.templates.createDefaultTemplate
import pt.rafap.kpb.core.templates.createDokkaTemplate

fun main() {
    val pfs = buildProject("kbp") {
        group("pt.rafap.kbp")

        val core = moduleProject("kbp-core", "core") {
            srcMainFile("Example.kt") {
                """
            package $group

            fun example(): String {
                return "This is an example from core module."
            }
            """.trimIndent()
            }
        }

        val cli = moduleProject("kbp-cli", "cli") {
            srcMainFile("Main.kt") {
                """
            package $group
            
            import pt.rafap.kbp.core.example

            fun main() {
                println(example())
            }
            """.trimIndent()
            }

            buildGradleModule {
                library("ktflag", "com.github.RafaPear:KtFlag") {
                    Version("ktflag", "1.5.4")
                }
                dependency {
                    Dependency("implementation(kotlin(\"stdlib\"))")
                }
                moduleGradle(core)
            }
        }

        val app = moduleProject("kbp-app", "app") {
            srcMainFile("Main.kt") {
                """
            package $group

            import androidx.compose.ui.window.*
            import pt.rafap.kbp.core.example
            
            fun main(args: Array<String>) {
                application {
                    val windowState = rememberWindowState(
                        placement = WindowPlacement.Floating,
                        position = WindowPosition.PlatformDefault
                    )
            
                    fun safeExitApplication() {
                        exitApplication()
                    }
            
                    Window(
                        onCloseRequest = ::safeExitApplication,
                        state = windowState,
                    ) {
            
                        window.minimumSize = java.awt.Dimension(500, 500)
                        println(example())
                    }
                }
            }
            
            """.trimIndent()
            }
            buildGradleModule {
                moduleGradle(core)
            }
        }

        fileProject("README.md") {
            """
            # KPB Project

            made using KPB - Kotlin Project Builder
            [read more](https://github.com/RafaPear/KPB)
            
            """.trimIndent()
        }

        templateProject {
            with(it) {
                addAllTemplates(
                    createDokkaTemplate(),
                    createDefaultTemplate("1.0.0"),
                    createAppTemplate(listOf(cli)),
                    createComposeTemplate(listOf(app))
                )
            }
        }
    }

    pfs.printStructure()

    pfs.createProject()
}