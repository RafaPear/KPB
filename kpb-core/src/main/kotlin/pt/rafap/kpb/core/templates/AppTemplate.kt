package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.GradleFile.Companion.buildGradleFile
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

fun Project.createAppTemplate(moduleNames: List<String>): Template {
    val template = this.buildTemplate {
        moduleNames.map { moduleName ->
            module(name = moduleName) {
                buildGradle {
                    otherPlugin("application", true)
                    other { createBuildGradleApp(moduleName, groupPath) }
                }
            }
        }
        handler { project ->
            project.copy(
                gradleFiles = project.gradleFiles +
                        buildGradleFile("build.gradle.kts") {
                            plugin(
                                "shadow",
                                "com.github.johnrengelman.shadow",
                                apply = false
                            ) {
                                Version("shadow", "8.1.1")
                            }
                            moduleNames.forEach { moduleName ->
                                other { createJarRunTask(moduleName) }
                            }
                        }
            )
        }
    }
    return template
}

private fun createJarRunTask(moduleName: String): String {
    val str = """
        val ${moduleName}Jar =
            tasks.register<Copy>("copy${moduleName}Jar") {
                dependsOn(":${moduleName}:build")
                from(project(":${moduleName}").layout.buildDirectory.dir("libs"))
                into(layout.buildDirectory.dir("libs"))
            }
        
        tasks.named("build") {
            dependsOn(${moduleName}Jar)
        }
    """.trimIndent()
    return str
}

private fun createBuildGradleApp(moduleName: String, group: String?): String {
    val newGroup = group ?: ""
    val str = """
    application {
        mainClass.set("${newGroup}.$moduleName.MainKt")
    }
    
    // === Fat JAR Task ===
    tasks.register<Jar>("fatJar") {
        group = "build"
        description = "Assembles a fat jar including all dependencies."
    
        // Nome final
        archiveBaseName.set(project.name)
        archiveVersion.set(rootProject.version.toString())
        archiveClassifier.set("") // sem "-all" → substitui o jar padrão
    
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    
        // Inclui classes do projeto
        from(sourceSets.main.get().output)
    
        // Inclui dependências (todas as libs do classpath)
        dependsOn(configurations.runtimeClasspath)
        from({
                 configurations.runtimeClasspath.get()
                     .filter { it.name.endsWith(".jar") }
                     .map { zipTree(it) }
             })
    
        // Manifest com Main-Class
        manifest {
            attributes["Main-Class"] = application.mainClass.get()
        }
    }
    
    kotlin {
        jvmToolchain(21)
    }
    
    // === Tornar o fatJar o padrão ===
    tasks {
        build {
            dependsOn("fatJar")
        }
    
        jar {
            enabled = true
        }
    }
    """.trimIndent()
    return str
}