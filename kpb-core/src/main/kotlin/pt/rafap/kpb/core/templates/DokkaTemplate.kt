package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

fun Project.createDokkaTemplate(): Template = this.buildTemplate {
    gradleFile("build.gradle.kts") {
        plugin("dokka", "org.jetbrains.dokka", apply = true) {
            Version("dokka", "2.1.0")
        }
        other {
                """
                buildscript { dependencies { classpath("org.jetbrains.dokka:dokka-base:2.0.0") } }
                
                allprojects {
                    repositories {
                        gradlePluginPortal()
                        mavenCentral()
                    }

                    apply(plugin = "org.jetbrains.dokka")
                    
                    dokka {
                        moduleName.set(project.name)

                        dokkaSourceSets.configureEach {
                            documentedVisibilities(
                                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Public, // Same for both Kotlin and Java
                                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Private, // Same for both Kotlin and Java
                                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Protected, // Same for both Kotlin and Java
                                org.jetbrains.dokka.gradle.engine.parameters.VisibilityModifier.Internal // Kotlin-specific internal modifier
                            )
                            val moduleDoc = file("MODULE.md")
                            if (moduleDoc.exists()) {
                                includes.from(moduleDoc)
                            }
                        }

                        dokkaPublications.html {
                            suppressInheritedMembers.set(true)
                            suppressObviousFunctions.set(true)
                        }

                        pluginsConfiguration.html {
                            footerMessage = "2025 Rafael Pereira"
                            separateInheritedMembers = false
                            mergeImplicitExpectActualDeclarations = true

                            val imagesDir = file("images")
                            if (imagesDir.exists()) {
                                customAssets.from(fileTree(imagesDir) { include("**/*.png") })
                            }
                        }
                    }
                }
                
                dokka {
                    moduleName.set(project.name)

                    dokkaPublications.html {
                        val packageDoc = rootProject.file("README.md")
                        if (packageDoc.exists()) {
                            includes.from(packageDoc)
                        }
                    }
                }

                dependencies {
                    for (project in subprojects) {
                        dokka(project)
                    }
                    dokka(rootProject)
                }
                
                tasks.named("build") {
                    dependsOn(tasks.dokkaGenerate)
                }
                """.trimIndent()
        }
    }
}