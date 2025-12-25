package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile.Companion.buildGradleFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

/**
 * Creates a template for Compose Desktop application modules.
 *
 * Configures modules with Jetpack Compose Multiplatform for desktop development.
 * Includes Compose compiler, runtime dependencies, and desktop-specific configuration.
 * Combines with the app template to provide executable JAR support.
 *
 * @param modules The list of modules to configure as Compose Desktop applications.
 * @return A Template configured for Compose Desktop with all necessary dependencies and plugins.
 */
fun Project.createComposeTemplate(modules: List<Module>): Template {
    var template = this.buildTemplate {
        modules.map { module ->
            moduleTemplate(module.name, module.simpleName) {
                buildGradleModule {
                    createModuleBuildGradleComposeApp(module)
                }
            }
        }
        handler { project ->
            project.copy(
                gradleFiles = project.gradleFiles +
                        buildGradleFile("build.gradle.kts") {
                            createBuildGradleComposeDesktopApp()
                        }
            )
        }
    }
    template += createAppTemplate(modules)
    return template
}

fun GradleFileBuildScope.createBuildGradleComposeDesktopApp() {
    plugin(
        "composeMultiplatform",
        "org.jetbrains.compose",
        apply = false
    ) {
        Version("composeMultiplatform", "1.9.1")
    }
    plugin(
        "composeCompiler",
        "org.jetbrains.kotlin.plugin.compose",
        apply = false
    ) {
        Version("kotlin", "2.2.20")
    }
    plugin(
        "composeHotReload",
        "org.jetbrains.compose.hot-reload",
        apply = false
    ) {
        Version("composeHotReload", "1.0.0-rc02")
    }
    other {
        """
        allprojects {
            repositories {
                gradlePluginPortal()
                mavenCentral()
                google()
                maven { url = uri("https://jitpack.io") }
            }
        }
        """.trimIndent()
    }
}

fun GradleFileBuildScope.createModuleBuildGradleComposeApp(module: Module) {
    plugin(
        "composeMultiplatform",
        "org.jetbrains.compose",
        apply = true
    ) {
        Version("composeMultiplatform", "1.9.1")
    }
    plugin(
        "composeCompiler",
        "org.jetbrains.kotlin.plugin.compose",
        apply = true
    ) {
        Version("kotlin", "2.2.20")
    }
    plugin(
        "composeHotReload",
        "org.jetbrains.compose.hot-reload",
        apply = true
    ) {
        Version("composeHotReload", "1.0.0-rc02")
    }
    dependency {
        Dependency("implementation(compose.desktop.currentOs)")
    }
    dependency {
        Dependency("implementation(compose.runtime)")
    }
    dependency {
        Dependency("implementation(compose.foundation)")
    }
    dependency {
        Dependency("implementation(compose.material3)")
    }
    dependency {
        Dependency("implementation(compose.ui)")
    }
    dependency {
        Dependency("implementation(compose.components.resources)")
    }
    dependency {
        Dependency("implementation(compose.components.uiToolingPreview)")
    }
    dependency {
        Dependency("implementation(libs.androidx.lifecycle.viewmodelCompose)")
    }
    dependency {
        Dependency("implementation(libs.androidx.lifecycle.runtimeCompose)")
    }
    dependency {
        Dependency("@OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)")
    }
    dependency {
        Dependency("implementation(compose.uiTest)")
    }
    dependency {
        Dependency("implementation(compose.materialIconsExtended)")
    }
    library(
        "androidx-lifecycle-viewmodelCompose",
        "org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose",
        write = false
    ) {
        Version("androidx-lifecycle", "2.9.5")
    }
    library(
        "androidx-lifecycle-runtimeCompose",
        "org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose",
        write = false
    ) {
        Version("androidx-lifecycle", "2.9.5")
    }
    import("org.jetbrains.compose.desktop.application.dsl.TargetFormat")
    other {
        """
                            compose.desktop {
                                application {
                                    mainClass = "${module.group}.MainKt"

                                    nativeDistributions {
                                        targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
                                        packageName = rootProject.name
                                        packageVersion = rootProject.version.toString()
                                        macOS {
                                            dockName = rootProject.name
                                            iconFile.set(project.file("src/main/composeResources/drawable/icon.png"))
                                        }
                                    }
                                }
                            }
                        """.trimIndent()
    }
}