package pt.rafap.kpb.cli

import pt.rafap.kpb.core.ProjectManager
import pt.rafap.kpb.core.TemplatePresets
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.utils.LogManager
import java.io.File
import kotlin.system.exitProcess

private val logger = LogManager.getLogger("CLI")

fun main() {
    LogManager.init(isCli = true)
    logger.info("Starting KPB CLI")
    System.out.flush()
    System.err.flush()

    mainMenu()
    logger.info("Exiting KPB CLI")
}


internal fun newProjectFlow(): ProjectManager? {
    val name = readInput("Project name: ") ?: return null
    logger.fine("User entered project name: '$name'")
    if (name.isBlank()) {
        println(error("Invalid")); return null
    }

    val pm = ProjectManager.create(name)
    logger.info("Created ProjectManager for '$name'")
    println(success("Project '$name' created"))

    val group = readInput("Group ID (e.g., com.example): ", allowBlank = true)
    logger.fine("User entered group: '$group'")
    if (!group.isNullOrBlank() && isValidGroup(group)) {
        pm.setGroup(group)
        logger.info("Set group to '$group'")
        println(success("Group set"))
    }

    moduleMenu(pm)
    return pm
}


internal fun saveFolderFlow(pm: ProjectManager) {
    val folder = readInput("Folder name: ") ?: return
    logger.fine("User entered save folder: '$folder'")
    if (folder.isBlank()) return
    try {
        logger.info("Attempting to save project to '$folder'")
        pm.saveConfigurationFolder(folder)
        logger.info("Saved project ${pm.project.name} to folder $folder")
        println(success("Saved to folder '$folder' (project.json + files/)"))
    } catch (e: Exception) {
        logger.severe("Save folder failed to $folder: ${e.message}")
        println(error("Save failed: ${e.message}"))
    }
}

internal fun loadFolderFlow(): ProjectManager? {
    val folder = readInput("Folder name: ") ?: return null
    logger.fine("User entered load folder: '$folder'")
    if (!ensureFolder(folder)) return null
    val newName = readInput("New name (blank to keep): ", allowBlank = true).takeUnless { it.isNullOrBlank() }
    val newGroup = readInput("New group (blank to keep): ", allowBlank = true).takeUnless { it.isNullOrBlank() }
    logger.fine("User entered newName='$newName', newGroup='$newGroup'")
    try {
        logger.info("Attempting to load project from '$folder'")
        val pm = ProjectManager.loadConfigurationFolder(folder, newName)
        newGroup?.let { pm.setGroup(it) }
        logger.info("Loaded project from folder $folder as ${pm.project.name} group=${pm.project.group}")
        println(success("Project loaded: ${pm.project.name}"))
        println(success("✓ Modules: ${pm.project.modules.size}"))
        println(success("✓ Version Catalog: ${pm.project.versionCatalog.versions.size} versions, ${pm.project.versionCatalog.libs.size} libs, ${pm.project.versionCatalog.plugins.size} plugins"))
        return pm
    } catch (e: Exception) {
        logger.severe("Load folder failed from $folder: ${e.message}")
        println(error("Load failed: ${e.message}"))
        return null
    }
}

internal fun generateFlow(pm: ProjectManager) {
    try {
        logger.info("Starting generation for project '${pm.project.name}'")
        pm.generate()
        logger.info("Generated project ${pm.project.name}")
        println(success("Generated: ${pm.project.name}"))
    } catch (e: Exception) {
        logger.severe("Generation failed: ${e.message}")
        println(error("Generation failed: ${e.message}"))
    }
}


internal fun addModuleFlow(pm: ProjectManager) {
    if (pm.project.group.isNullOrBlank()) {
        println(error("Set group first"))
        return
    }

    val name = readInput("Module name: ") ?: return
    val simple = readInput("Simple name: ") ?: return
    logger.fine("User entered module name='$name', simple='$simple'")

    try {
        pm.addModule(name, simple)
        logger.info("Added module $name ($simple)")
        println(success("Module added"))
    } catch (e: Exception) {
        logger.severe("Add module failed for $name: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

internal fun deleteModuleFlow(pm: ProjectManager) {
    val modules = pm.project.modules
    if (modules.isEmpty()) {
        println(error("No modules")); return
    }

    val idx = readInput("Index to delete: ")?.toIntOrNull() ?: return
    logger.fine("User entered delete index: $idx")
    if (idx !in modules.indices) {
        println(error("Invalid")); return
    }

    try {
        val name = modules[idx].name
        logger.info("Deleting module '$name' at index $idx")
        pm.removeModule(name)
        logger.info("Deleted module $name")
        println(success("Deleted"))
    } catch (e: Exception) {
        logger.severe("Delete module failed: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

internal fun editModuleFlow(pm: ProjectManager) {
    val modules = pm.project.modules
    if (modules.isEmpty()) {
        println(error("No modules")); return
    }

    val idx = readInput("Index to edit: ")?.toIntOrNull() ?: return
    logger.fine("User entered edit index: $idx")
    if (idx !in modules.indices) {
        println(error("Invalid")); return
    }

    val module = modules[idx]
    logger.info("Editing module '${module.name}'")
    editModuleSubmenu(module, pm)
}

private fun editModuleSubmenu(module: Module, pm: ProjectManager) {
    while (true) {
        println()
        println(title("=== ${module.name} ==="))
        println(menu("  1") + " - Add Source File")
        println(menu("  2") + " - Add Resource File")
        println(menu("  3") + " - Add Library")
        println(menu("  4") + " - Add Plugin")
        println(menu("  5") + " - Add Dependency")
        println(menu("  6") + " - Add Module Dependency")
        println(menu("  7") + " - Apply Preset Template")
        println(menu("  8") + " - Show Variables")
        println(menu("  H") + " - Help")
        println(menu("  0") + " - Back")
        println()
        print(highlight("Choose: "))

        val choice = readlnOrNull()?.trim()?.uppercase()
        logger.fine("User chose edit option: '$choice' for module '${module.name}'")
        when (choice) {
            "1" -> addSourceFileFlow(module, pm)
            "2" -> addResourceFileFlow(module, pm)
            "3" -> addLibraryFlow(module, pm)
            "4" -> addPluginFlow(module, pm)
            "5" -> addDependencyFlow(module, pm)
            "6" -> addModuleDepFlow(module, pm)
            "7" -> applyPresetFlow(module, pm)
            "8" -> showVariables(module, pm)
            "H" -> showEditModuleHelp()
            "0" -> return
            else -> println(error("Invalid"))
        }
    }
}

private fun showEditModuleHelp() {
    println()
    println(title("=== Edit Module Help ==="))
    println("  ${Colors.BOLD}Add Source File:${Colors.RESET} Add a Kotlin source file to src/main/kotlin.")
    println("  ${Colors.BOLD}Add Resource File:${Colors.RESET} Add a resource file to src/main/resources.")
    println("  ${Colors.BOLD}Add Library:${Colors.RESET} Add a library dependency to the version catalog and module.")
    println("  ${Colors.BOLD}Add Plugin:${Colors.RESET} Add a Gradle plugin to the version catalog and module.")
    println("  ${Colors.BOLD}Add Dependency:${Colors.RESET} Add a raw dependency string (e.g., implementation(...)).")
    println("  ${Colors.BOLD}Add Module Dependency:${Colors.RESET} Make this module depend on another module in the project.")
    println("  ${Colors.BOLD}Apply Preset Template:${Colors.RESET} Apply a file preset (e.g., Main.kt, README.md).")
    println("  ${Colors.BOLD}Show Variables:${Colors.RESET} Show available variables for template interpolation.")
    println("  ${Colors.BOLD}Back:${Colors.RESET} Return to the module menu.")
    println()
    readInput("Press Enter to continue...", allowBlank = true)
}

private fun addSourceFileFlow(module: Module, pm: ProjectManager) {
    val path = readInput("File path (e.g., Main.kt): ") ?: return
    logger.fine("User entered source file path: '$path'")

    print(highlight("Content (type 'cancel' to abort):\n"))
    val content = readContent() ?: return
    logger.fine("User entered content length: ${content.length}")

    try {
        val interpolated = interpolateContent(content, module, pm.project)
        logger.fine("Interpolated content length: ${interpolated.length}")
        pm.addSourceFile(module.name, path, interpolated)
        logger.info("Added source file ${module.name}/$path")
        println(success("File added"))
    } catch (e: Exception) {
        logger.severe("Add source file failed ${module.name}/$path: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun addResourceFileFlow(module: Module, pm: ProjectManager) {
    val path = readInput("File path: ") ?: return

    print(highlight("Content (type 'cancel' to abort):\n"))
    val content = readContent() ?: return

    try {
        val interpolated = interpolateContent(content, module, pm.project)
        pm.addResourceFile(module.name, path, interpolated)
        logger.info("Added resource file ${module.name}/$path")
        println(success("File added"))
    } catch (e: Exception) {
        logger.severe("Add resource file failed ${module.name}/$path: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun addLibraryFlow(module: Module, pm: ProjectManager) {
    val alias = readInput("Library alias: ") ?: return
    val id = readInput("Library ID (group:artifact): ") ?: return
    val verName = readInput("Version name: ") ?: return
    val verVal = readInput("Version value: ") ?: return

    try {
        pm.addLibrary(module.name, alias, id, verName, verVal)
        logger.info("Added library $alias -> $id ($verName=$verVal) to ${module.name}")
        println(success("Library added"))
    } catch (e: Exception) {
        logger.severe("Add library failed for ${module.name}: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun addPluginFlow(module: Module, pm: ProjectManager) {
    val alias = readInput("Plugin alias: ") ?: return
    val id = readInput("Plugin ID: ") ?: return
    val verName = readInput("Version name: ") ?: return
    val verVal = readInput("Version value: ") ?: return

    try {
        pm.addPlugin(module.name, alias, id, verName, verVal)
        logger.info("Added plugin $alias -> $id ($verName=$verVal) to ${module.name}")
        println(success("Plugin added"))
    } catch (e: Exception) {
        logger.severe("Add plugin failed for ${module.name}: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun addDependencyFlow(module: Module, pm: ProjectManager) {
    val dep = readInput("Dependency (e.g., implementation(kotlin(\"stdlib\"))): ") ?: return

    try {
        pm.addDependency(module.name, dep)
        logger.info("Added dependency to ${module.name}: $dep")
        println(success("Dependency added"))
    } catch (e: Exception) {
        logger.severe("Add dependency failed for ${module.name}: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun addModuleDepFlow(module: Module, pm: ProjectManager) {
    val others = pm.project.modules.filter { it.name != module.name }
    if (others.isEmpty()) {
        println(error("No other modules")); return
    }

    println(info("Available modules:"))
    others.forEachIndexed { i, m -> println("  $i - ${m.name}") }

    val idx = readInput("Select: ")?.toIntOrNull() ?: return
    if (idx !in others.indices) {
        println(error("Invalid")); return
    }

    try {
        pm.addModuleDependency(module.name, others[idx].name)
        logger.info("Added module dependency ${module.name} -> ${others[idx].name}")
        println(success("Dependency added"))
    } catch (e: Exception) {
        logger.severe("Add module dependency failed: ${e.message}")
        println(error(e.message ?: "Failed"))
    }
}

private fun applyPresetFlow(module: Module, pm: ProjectManager) {
    println(info("Presets:"))
    println("  1 - Compose Desktop")
    println("  2 - CLI App")
    println("  3 - Library API")
    println("  4 - Module README")
    println("  5 - .gitignore")

    when (readInput("Select: ")?.trim()) {
        "1" -> {
            val content = TemplatePresets.composeDesktopMainKt(module.group, module.name, pm.project.name)
            pm.addSourceFile(module.name, "Main.kt", content)
            pm.applyComposeTemplate(listOf(module.name))
            logger.info("Applied Compose Desktop preset to ${module.name}")
            println(success("Template applied"))
        }

        "2" -> {
            val content = TemplatePresets.cliMainKt(module.group, module.name, pm.project.name)
            pm.addSourceFile(module.name, "Main.kt", content)
            pm.applyCliTemplate(listOf(module.name))
            logger.info("Applied CLI preset to ${module.name}")
            println(success("Template applied"))
        }

        "3" -> {
            val baseGroup = module.group.removeSuffix(".${module.simpleName}")
            val content = TemplatePresets.libraryMainKt(baseGroup, module.simpleName)
            pm.addSourceFile(module.name, "Api.kt", content)
            logger.info("Applied Library preset to ${module.name}")
            println(success("Template applied"))
        }

        "4" -> {
            val content = TemplatePresets.moduleReadme(module.name, pm.project.name)
            pm.addResourceFile(module.name, "README.md", content)
            logger.info("Applied README preset to ${module.name}")
            println(success("Template applied"))
        }

        "5" -> {
            val content = TemplatePresets.gitignore()
            pm.addResourceFile(module.name, ".gitignore", content)
            logger.info("Applied .gitignore preset to ${module.name}")
            println(success("Template applied"))
        }
    }
}

private fun showVariables(module: Module, pm: ProjectManager) {
    println(
        $$"""
        
$${Colors.BOLD}Available Variables:$${Colors.RESET}

Module:
  ${MODULE_GROUP} → $${module.group}
  ${MODULE_NAME} → $${module.name}
  ${MODULE_SIMPLE_NAME} → $${module.simpleName}

Project:
  ${PROJECT_NAME} → $${pm.project.name}
  ${PROJECT_GROUP} → $${pm.project.group}

Conversions: _CAMEL, _PASCAL

Example: ${MODULE_NAME_PASCAL}, ${PROJECT_NAME_CAMEL}
    """.trimIndent()
    )
}


internal fun readInput(prompt: String, allowBlank: Boolean = false): String? {
    print(highlight(prompt))
    val line = readlnOrNull()
    if (line == null) {
        println()
        println(info("Exiting (no input)"))
        exitProcess(0)
    }
    val trimmed = line.trim()
    return if (!allowBlank && trimmed.isBlank()) null else trimmed
}


private fun ensureFolder(path: String): Boolean {
    val f = File(path)
    if (!f.exists() || !f.isDirectory) {
        println(error("Folder not found: $path"))
        return false
    }
    return true
}

internal fun viewProjectStructure(pm: ProjectManager) {
    val p = pm.project
    println()
    println(title("═══════════════════════════════════════"))
    println(title("  PROJECT: ${p.name}"))
    println(title("═══════════════════════════════════════"))
    println()
    println("${Colors.BOLD}General:${Colors.RESET}")
    println("  Name:  ${p.name}")
    println("  Group: ${p.group ?: "(not set)"}")
    println()

    if (p.versionCatalog.versions.isNotEmpty()) {
        println("${Colors.BOLD}Versions (${p.versionCatalog.versions.size}):${Colors.RESET}")
        p.versionCatalog.versions.forEach { v -> println("  ${Colors.YELLOW}${v.name}${Colors.RESET} → ${v.version}") }
        println()
    }

    if (p.versionCatalog.libs.isNotEmpty()) {
        println("${Colors.BOLD}Libraries (${p.versionCatalog.libs.size}):${Colors.RESET}")
        p.versionCatalog.libs.forEach { lib -> println("  ${Colors.CYAN}${lib.name}${Colors.RESET} → ${lib.id}") }
        println()
    }

    if (p.versionCatalog.plugins.isNotEmpty()) {
        println("${Colors.BOLD}Plugins (${p.versionCatalog.plugins.size}):${Colors.RESET}")
        p.versionCatalog.plugins.forEach { plugin -> println("  ${Colors.MAGENTA}${plugin.name}${Colors.RESET} → ${plugin.id}") }
        println()
    }

    if (p.modules.isNotEmpty()) {
        println("${Colors.BOLD}Modules (${p.modules.size}):${Colors.RESET}")
        p.modules.forEachIndexed { idx, m ->
            println()
            println("  ${Colors.BLUE}[$idx] ${m.name}${Colors.RESET}")
            println("      Simple:  ${m.simpleName}")
            println("      Group:   ${m.group}")
            if (m.files.isNotEmpty()) {
                println("      Files (${m.files.size}):")
                m.files.forEach { f ->
                    val lines = (f.content?.count { it == '\n' } ?: 0) + 1
                    println("        • ${f.path} ($lines lines)")
                }
            }
        }
        println()
    } else {
        println("  ${Colors.RED}No modules${Colors.RESET}")
        println()
    }

    println(title("═══════════════════════════════════════"))
    println()
}

private fun interpolateContent(template: String, module: Module, project: Project): String {
    val baseGroup = module.group.removeSuffix(".${module.simpleName}")
    fun toCamelCase(str: String) =
        str.split("-", "_").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }

    return template
        .replace($$"${MODULE_GROUP}", module.group)
        .replace($$"${MODULE_NAME}", module.name)
        .replace($$"${MODULE_NAME_CAMEL}", toCamelCase(module.name))
        .replace($$"${MODULE_NAME_PASCAL}", toCamelCase(module.name))
        .replace($$"${MODULE_SIMPLE_NAME}", module.simpleName)
        .replace($$"${MODULE_SIMPLE_NAME_CAMEL}", toCamelCase(module.simpleName))
        .replace($$"${MODULE_SIMPLE_NAME_PASCAL}", toCamelCase(module.simpleName))
        .replace($$"${GROUP_BASE}", baseGroup)
        .replace($$"${PROJECT_NAME}", project.name)
        .replace($$"${PROJECT_NAME_CAMEL}", toCamelCase(project.name))
        .replace($$"${PROJECT_NAME_PASCAL}", toCamelCase(project.name))
        .replace($$"${PROJECT_GROUP}", project.group ?: baseGroup)
        .replace($$"${YEAR}", java.time.LocalDate.now().year.toString())
}

private fun isValidGroup(group: String): Boolean {
    return group.isNotBlank() && !group.contains(" ") && !group.endsWith(".") &&
            !group.startsWith(".") && !group.contains("..") && !group.contains("/")
}

private fun readContent(): String? {
    println(info("Type 'END' on a new line to finish, or 'cancel' to abort."))
    val lines = mutableListOf<String>()
    while (true) {
        val line = readlnOrNull() ?: break
        if (line.trim() == "cancel") return null
        if (line.trim() == "END") break
        lines.add(line)
    }
    return lines.joinToString("\n")
}
