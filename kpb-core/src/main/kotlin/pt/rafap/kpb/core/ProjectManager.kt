package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.content.Dependency
import pt.rafap.kpb.core.gradle.content.Version
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.module.Module.Companion.buildModule
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.project.Project.Companion.buildProject
import pt.rafap.kpb.core.templates.*
import pt.rafap.kpb.utils.LogManager

private val logger = LogManager.getLogger("ProjectManager")

/**
 * A high-level facade to manage KPB Projects without directly using the DSL.
 *
 * Provides convenient methods to mutate an in-memory Project and generate it.
 * Internally leverages the existing DSL to ensure compatibility and correctness.
 */
@Suppress("unused")
class ProjectManager private constructor(var project: Project) {

    // === Project-level ===
    fun setGroup(groupId: String): ProjectManager {
        require(groupId.isNotBlank()) { "Group cannot be blank" }
        logger.info("Setting project group to '$groupId'")
        val updated = buildProject(project.name) { group(groupId) } + project
        val newModules = updated.modules.map { it.copy(group = "$groupId.${it.simpleName}") }
        project = updated.copy(modules = newModules)
        return this
    }

    fun addReadme(content: String = defaultReadme()): ProjectManager {
        logger.info("Adding README to project")
        val updated = project + buildProject(project.name) {
            fileProject("README.md") { content }
        }
        project = updated
        return this
    }

    fun printStructure(): ProjectManager {
        project.printStructure(); return this
    }

    fun generate(path: String = ""): ProjectManager {
        logger.info("Generating project '${project.name}'")
        project.parseProject().createProject(path)
        return this
    }

    // === Module-level ===
    fun addModule(name: String, simpleName: String): ProjectManager {
        logger.info("Adding module '$name' (simple: '$simpleName')")
        val group = project.group ?: throw IllegalStateException("Set project group before adding modules")
        require(project.modules.none { it.name == name }) { "Module '$name' already exists" }
        require(project.modules.none { it.simpleName == simpleName }) { "Module simpleName '$simpleName' already exists" }
        val updated = project + buildProject(project.name) {
            group(group)
            moduleProject(name, simpleName) {}
        }
        project = updated
        return this
    }

    fun removeModule(name: String): ProjectManager {
        logger.info("Removing module '$name'")
        val module = project.modules.find { it.name == name }
            ?: throw IllegalArgumentException("Module '$name' not found")
        project -= module
        logger.fine("Module '$name' removed from project")
        return this
    }

    fun addSourceFile(moduleName: String, path: String, content: String): ProjectManager {
        logger.fine("Adding source file to '$moduleName' at '$path' (length=${content.length})")
        val module = getModuleOrThrow(moduleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            srcMainFile(path) { content }
        }
        project += patch
        logger.info("Added source file '$path' to module '$moduleName'")
        return this
    }

    fun addResourceFile(moduleName: String, path: String, content: String): ProjectManager {
        logger.fine("Adding resource file to '$moduleName' at '$path' (length=${content.length})")
        val module = getModuleOrThrow(moduleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            resourceFile(path) { content }
        }
        project += patch
        logger.info("Added resource file '$path' to module '$moduleName'")
        return this
    }

    fun addLibrary(
        moduleName: String,
        alias: String,
        id: String,
        versionName: String,
        versionValue: String
    ): ProjectManager {
        logger.fine("Adding library to '$moduleName': alias='$alias', id='$id', version='$versionName'='$versionValue'")
        val module = getModuleOrThrow(moduleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            buildGradleModule {
                library(alias, id) { Version(versionName, versionValue) }
            }
        }
        project += patch
        logger.info("Added library '$alias' to module '$moduleName'")
        return this
    }

    fun addPlugin(
        moduleName: String,
        alias: String,
        pluginId: String,
        versionName: String,
        versionValue: String,
        apply: Boolean = true
    ): ProjectManager {
        logger.fine("Adding plugin to '$moduleName': alias='$alias', id='$pluginId', version='$versionName'='$versionValue', apply=$apply")
        val module = getModuleOrThrow(moduleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            buildGradleModule {
                plugin(alias, pluginId, apply) { Version(versionName, versionValue) }
            }
        }
        project += patch
        logger.info("Added plugin '$alias' to module '$moduleName'")
        return this
    }

    fun addDependency(moduleName: String, dependencyString: String): ProjectManager {
        logger.fine("Adding dependency to '$moduleName': '$dependencyString'")
        val module = getModuleOrThrow(moduleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            buildGradleModule {
                dependency { Dependency(dependencyString) }
            }
        }
        project += patch
        logger.info("Added dependency to module '$moduleName'")
        return this
    }

    fun addModuleDependency(moduleName: String, dependsOnModuleName: String): ProjectManager {
        logger.fine("Adding module dependency: '$moduleName' depends on '$dependsOnModuleName'")
        val module = getModuleOrThrow(moduleName)
        val dep = getModuleOrThrow(dependsOnModuleName)
        val baseGroup = module.group.removeSuffix(".${module.simpleName}")
        val patch = buildModule(module.name, module.simpleName, baseGroup) {
            buildGradleModule {
                moduleGradle(dep)
            }
        }
        project += patch
        logger.info("Added module dependency '$moduleName' -> '$dependsOnModuleName'")
        return this
    }

    // === Templates ===
    fun applyDokkaTemplate(): ProjectManager {
        logger.info("Applying Dokka template")
        project += project.createDokkaTemplate()
        return this
    }

    fun applyDefaultTemplate(version: String): ProjectManager {
        logger.info("Applying Default template with version '$version'")
        require(version.isNotBlank()) { "Version cannot be blank" }
        project += project.createDefaultTemplate(version)
        return this
    }

    fun applyAppTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying App template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createAppTemplate(ms) }
        return this
    }

    fun applyComposeTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying Compose template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createComposeTemplate(ms) }
        return this
    }

    fun applyCliTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying CLI template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createCliTemplate(ms) }
        return this
    }

    fun applyCoroutinesTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying Coroutines template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createCoroutinesTemplate(ms) }
        return this
    }

    fun applySerializationTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying Serialization template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createSerializationTemplate(ms) }
        return this
    }

    fun applyTestTemplate(modules: List<String>): ProjectManager {
        logger.info("Applying Test template to modules: $modules")
        val mods = modules.map { getModuleOrThrow(it) }
        applyTemplateWithGroup(mods) { p, ms -> p.createTestTemplate(ms) }
        return this
    }

    fun applyGitHubWorkflowsTemplate(
        includeDocs: Boolean,
        includeTests: Boolean,
        includeArtifacts: Boolean
    ): ProjectManager {
        logger.info("Applying GitHub Workflows template: docs=$includeDocs, tests=$includeTests, artifacts=$includeArtifacts")
        if (includeDocs) {
            project += project.createDocsWorkflowTemplate()
        }
        if (includeTests) {
            project += project.createTestsWorkflowTemplate()
        }
        if (includeArtifacts) {
            project += project.createArtifactsWorkflowTemplate()
        }
        return this
    }

    private fun applyTemplateWithGroup(mods: List<Module>, builder: (Project, List<Module>) -> Template) {
        if (mods.isEmpty()) return
        fun baseGroup(m: Module) = m.group.removeSuffix(".${m.simpleName}")
        val base = baseGroup(mods.first())
        require(mods.all { baseGroup(it) == base }) { "Selected modules must share the same base group." }
        val temp = project.copy(group = base)
        val tpl = builder(temp, mods)
        project += tpl
    }

    private fun getModuleOrThrow(name: String): Module {
        return project.modules.find { it.name == name }
            ?: throw IllegalArgumentException("Module '$name' not found")
    }

    private fun defaultReadme(): String = """
        # KPB Project

        made using KPB - Kotlin Project Builder
        [read more](https://github.com/RafaPear/KPB)
    """.trimIndent()

    fun saveConfiguration(filename: String): ProjectManager {
        logger.info("Saving configuration to '$filename'")
        val result = ProjectPersistence.save(this, filename)
        if (result.isFailure) {
            val ex = result.exceptionOrNull() ?: Exception("Failed to save configuration")
            logger.severe("Failed to save configuration: ${ex.message}")
            throw ex
        }
        logger.fine("Configuration saved successfully")
        return this
    }

    fun saveConfigurationFolder(folderName: String): ProjectManager {
        logger.info("Saving configuration folder to '$folderName'")
        val result = ProjectPersistence.saveToFolder(this, folderName)
        if (result.isFailure) {
            val ex = result.exceptionOrNull() ?: Exception("Failed to save configuration")
            logger.severe("Failed to save configuration folder: ${ex.message}")
            throw ex
        }
        logger.fine("Configuration folder saved successfully")
        return this
    }

    fun applyVersionCatalog(versionCatalog: pt.rafap.kpb.core.gradle.VersionCatalog): ProjectManager {
        logger.info("Applying version catalog")
        project = project.copy(versionCatalog = project.versionCatalog + versionCatalog)
        return this
    }

    companion object {
        fun create(name: String): ProjectManager {
            logger.info("Creating new ProjectManager for '$name'")
            return ProjectManager(buildProject(name) {})
        }

        fun from(project: Project): ProjectManager {
            logger.info("Creating ProjectManager from existing project '${project.name}'")
            return ProjectManager(project)
        }

        fun loadConfiguration(filename: String, newName: String? = null): ProjectManager {
            logger.info("Loading configuration from '$filename' (newName=$newName)")
            val result = ProjectPersistence.load(filename, newName)
            if (result.isFailure) {
                val ex = result.exceptionOrNull() ?: Exception("Failed to load configuration")
                logger.severe("Failed to load configuration: ${ex.message}")
                throw ex
            }
            return result.getOrNull() ?: throw Exception("Failed to load configuration")
        }

        fun loadConfigurationFolder(
            folderName: String,
            newName: String? = null,
            newGroup: String? = null
        ): ProjectManager {
            logger.info("Loading configuration folder from '$folderName' (newName=$newName, newGroup=$newGroup)")
            val result = ProjectPersistence.loadFromFolder(folderName, newName, newGroup)
            if (result.isFailure) {
                val ex = result.exceptionOrNull() ?: Exception("Failed to load configuration")
                logger.severe("Failed to load configuration folder: ${ex.message}")
                throw ex
            }
            return result.getOrNull() ?: throw Exception("Failed to load configuration")
        }
    }
}
