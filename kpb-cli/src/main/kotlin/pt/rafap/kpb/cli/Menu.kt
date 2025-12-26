package pt.rafap.kpb.cli

import pt.rafap.kpb.core.ProjectManager

// ANSI Colors
internal object Colors {
    private val enabled: Boolean = System.getenv("NO_COLOR") == null
    val RESET = if (enabled) "\u001B[0m" else ""
    val BOLD = if (enabled) "\u001B[1m" else ""
    val CYAN = if (enabled) "\u001B[36m" else ""
    val GREEN = if (enabled) "\u001B[32m" else ""
    val YELLOW = if (enabled) "\u001B[33m" else ""
    val RED = if (enabled) "\u001B[31m" else ""
    val BLUE = if (enabled) "\u001B[34m" else ""
    val MAGENTA = if (enabled) "\u001B[35m" else ""
}

internal fun title(text: String) = "${Colors.BOLD}${Colors.CYAN}$text${Colors.RESET}"
internal fun success(text: String) = "${Colors.GREEN}✓ $text${Colors.RESET}"
internal fun error(text: String) = "${Colors.RED}✗ $text${Colors.RESET}"
internal fun info(text: String) = "${Colors.BLUE}ℹ $text${Colors.RESET}"
internal fun menu(text: String) = "${Colors.YELLOW}$text${Colors.RESET}"
internal fun highlight(text: String) = "${Colors.BOLD}${Colors.CYAN}$text${Colors.RESET}"

internal fun mainMenu() {
    var pm: ProjectManager? = null
    System.out.flush()
    while (true) {
        println()
        println(title("╔════════════════════════════════════════╗"))
        println(title("║       KPB - Kotlin Project Builder     ║"))
        println(title("╚════════════════════════════════════════╝"))
        println()

        if (pm != null) {
            println("${Colors.GREEN}[${pm.project.name}]${Colors.RESET} @ ${Colors.BLUE}${pm.project.group}${Colors.RESET}")
        }
        println()
        println(menu("  N") + " - New Project")
        println(menu("  L") + " - Load Project")
        println(menu("  M") + " - Manage Modules")
        println(menu("  T") + " - Apply Templates")
        println(menu("  S") + " - Save Project")
        println(menu("  G") + " - Generate to Disk")
        println(menu("  V") + " - View Structure")
        println(menu("  P") + " - Project Variables")
        println(menu("  H") + " - Help")
        println(menu("  Q") + " - Quit")
        println()
        print(highlight("Choose: "))

        when (readlnOrNull()?.trim()?.uppercase()) {
            "N" -> pm = newProjectFlow()
            "L" -> pm = loadFolderFlow()
            "M" -> if (pm != null) moduleMenu(pm) else println(error("No project loaded"))
            "T" -> if (pm != null) templateMenu(pm) else println(error("No project loaded"))
            "S" -> if (pm != null) saveFolderFlow(pm) else println(error("No project loaded"))
            "G" -> if (pm != null) generateFlow(pm) else println(error("No project loaded"))
            "V" -> if (pm != null) viewProjectStructure(pm) else println(error("No project loaded"))
            "P" -> if (pm != null) projectVariablesMenu(pm) else println(error("No project loaded"))
            "H" -> showMainHelp()
            "Q" -> {
                println()
                println(title("👋 Goodbye!"))
                break
            }

            else -> println(error("Invalid option"))
        }
    }
}

internal fun moduleMenu(pm: ProjectManager) {
    while (true) {
        println()
        println(title("=== Modules ==="))

        val modules = pm.project.modules
        if (modules.isNotEmpty()) {
            modules.forEachIndexed { i, m ->
                println("  ${Colors.MAGENTA}$i${Colors.RESET} - ${m.name}")
            }
            println()
        }

        println(menu("  A") + " - Add Module")
        println(menu("  E") + " - Edit Module")
        println(menu("  D") + " - Delete Module")
        println(menu("  H") + " - Help")
        println(menu("  0") + " - Back")
        println()
        print(highlight("Choose: "))

        when (readlnOrNull()?.trim()?.uppercase()) {
            "A" -> addModuleFlow(pm)
            "E" -> editModuleFlow(pm)
            "D" -> deleteModuleFlow(pm)
            "H" -> showModuleHelp()
            "0" -> return
            else -> println(error("Invalid"))
        }
    }
}

internal fun templateMenu(pm: ProjectManager) {
    while (true) {
        println()
        println(title("=== Templates ==="))
        println(menu("  1") + " - Kotlin App")
        println(menu("  2") + " - Compose Desktop")
        println(menu("  3") + " - Dokka Docs")
        println(menu("  4") + " - Default (with version)")
        println(menu("  5") + " - CLI App")
        println(menu("  6") + " - Coroutines")
        println(menu("  7") + " - Serialization")
        println(menu("  8") + " - Test")
        println(menu("  9") + " - GitHub Workflows")
        println(menu("  H") + " - Help")
        println(menu("  0") + " - Back")
        println()
        print(highlight("Choose: "))

        when (readInput("Choose: ")?.trim()?.uppercase()) {
            "1" -> selectModulesAndApply(pm) { pm.applyAppTemplate(it) }
            "2" -> selectModulesAndApply(pm) { pm.applyComposeTemplate(it) }
            "3" -> {
                try {
                    pm.applyDokkaTemplate()
                    println(success("Dokka template applied"))
                } catch (e: Exception) {
                    println(error(e.message ?: "Failed"))
                }
            }

            "4" -> {
                val version = readInput("Version: ") ?: return
                if (version.isNotBlank()) {
                    try {
                        pm.applyDefaultTemplate(version)
                        println(success("Default template applied"))
                    } catch (e: Exception) {
                        println(error(e.message ?: "Failed"))
                    }
                }
            }

            "5" -> selectModulesAndApply(pm) { pm.applyCliTemplate(it) }
            "6" -> selectModulesAndApply(pm) { pm.applyCoroutinesTemplate(it) }
            "7" -> selectModulesAndApply(pm) { pm.applySerializationTemplate(it) }
            "8" -> selectModulesAndApply(pm) { pm.applyTestTemplate(it) }
            "9" -> {
                val docs = readInput("Include Dokka docs workflow? (y/n): ")?.lowercase() == "y"
                val tests = readInput("Include Tests workflow? (y/n): ")?.lowercase() == "y"
                val artifacts = readInput("Include Artifacts workflow? (y/n): ")?.lowercase() == "y"
                try {
                    pm.applyGitHubWorkflowsTemplate(docs, tests, artifacts)
                    println(success("GitHub Workflows template applied"))
                } catch (e: Exception) {
                    println(error(e.message ?: "Failed"))
                }
            }

            "H" -> showTemplateHelp()
            "0" -> return
            else -> println(error("Invalid"))
        }
    }
}

internal fun projectVariablesMenu(pm: ProjectManager) {
    while (true) {
        println()
        println(title("=== Project Variables ==="))
        println(info("Current name: ${pm.project.name}"))
        println(info("Current group: ${pm.project.group ?: "<not set>"}"))
        println(menu("  1") + " - Rename project")
        println(menu("  2") + " - Set group")
        println(menu("  H") + " - Help")
        println(menu("  0") + " - Back")
        print(highlight("Choose: "))
        when (readInput("Choose: ")?.trim()?.uppercase()) {
            "1" -> {
                val newName = readInput("New project name: ")
                if (!newName.isNullOrBlank()) {
                    pm.project = pm.project.copy(name = newName)
                    println(success("Project renamed to $newName"))
                }
            }

            "2" -> {
                val newGroup = readInput("New group: ")
                if (!newGroup.isNullOrBlank()) {
                    try {
                        pm.setGroup(newGroup)
                        println(success("Group set to $newGroup"))
                    } catch (e: Exception) {
                        println(error(e.message ?: "Failed"))
                    }
                }
            }

            "H" -> showProjectVariablesHelp()
            "0" -> return
            else -> println(error("Invalid"))
        }
    }
}

private fun selectModulesAndApply(pm: ProjectManager, action: (List<String>) -> Unit) {
    val modules = pm.project.modules
    if (modules.isEmpty()) {
        println(error("No modules")); return
    }

    println(info("Select modules (space-separated):"))
    modules.forEachIndexed { i, m -> println("  $i - ${m.name}") }

    val selected = readInput("Indices: ")?.trim()?.split(" ")?.mapNotNull { it.toIntOrNull() }
        ?.mapNotNull { modules.getOrNull(it) } ?: return
    if (selected.isEmpty()) {
        println(error("No modules selected")); return
    }

    try {
        action(selected.map { it.name })
        println(success("Template applied"))
    } catch (e: Exception) {
        println(error(e.message ?: "Failed"))
    }
}

private fun showMainHelp() {
    println()
    println(title("=== Main Menu Help ==="))
    println("  ${Colors.BOLD}New Project:${Colors.RESET} Create a new project from scratch.")
    println("  ${Colors.BOLD}Load Project:${Colors.RESET} Load an existing project from a folder.")
    println("  ${Colors.BOLD}Manage Modules:${Colors.RESET} Add, edit, or delete modules in the project.")
    println("  ${Colors.BOLD}Apply Templates:${Colors.RESET} Apply pre-configured templates to modules.")
    println("  ${Colors.BOLD}Save Project:${Colors.RESET} Save the project configuration to a folder.")
    println("  ${Colors.BOLD}Generate to Disk:${Colors.RESET} Generate the project files to disk.")
    println("  ${Colors.BOLD}View Structure:${Colors.RESET} View the current project structure.")
    println("  ${Colors.BOLD}Project Variables:${Colors.RESET} Manage project-wide variables like name and group.")
    println("  ${Colors.BOLD}Quit:${Colors.RESET} Exit the application.")
    println()
    println("  ${Colors.BOLD}How to use:${Colors.RESET}")
    println("  1. Create a new project or load an existing one.")
    println("  2. Add modules to the project.")
    println("  3. Apply templates to the modules to add functionality.")
    println("  4. Generate the project to disk.")
    println()
    readInput("Press Enter to continue...", allowBlank = true)
}

private fun showModuleHelp() {
    println()
    println(title("=== Module Menu Help ==="))
    println("  ${Colors.BOLD}Add Module:${Colors.RESET} Add a new module to the project.")
    println("  ${Colors.BOLD}Edit Module:${Colors.RESET} Edit an existing module (add files, dependencies, etc.).")
    println("  ${Colors.BOLD}Delete Module:${Colors.RESET} Remove a module from the project.")
    println("  ${Colors.BOLD}Back:${Colors.RESET} Return to the main menu.")
    println()
    readInput("Press Enter to continue...", allowBlank = true)
}

private fun showTemplateHelp() {
    println()
    println(title("=== Template Menu Help ==="))
    println("  ${Colors.BOLD}Kotlin App:${Colors.RESET} Configures modules as executable applications with Shadow JAR support.")
    println("  ${Colors.BOLD}Compose Desktop:${Colors.RESET} Adds Compose Desktop support to modules.")
    println("  ${Colors.BOLD}Dokka Docs:${Colors.RESET} Configures Dokka for API documentation generation.")
    println("  ${Colors.BOLD}Default:${Colors.RESET} Applies default configuration with a specific Kotlin version.")
    println("  ${Colors.BOLD}CLI App:${Colors.RESET} Adds CLI support using KtFlag.")
    println("  ${Colors.BOLD}Coroutines:${Colors.RESET} Adds Kotlin Coroutines support.")
    println("  ${Colors.BOLD}Serialization:${Colors.RESET} Adds Kotlin Serialization support.")
    println("  ${Colors.BOLD}Test:${Colors.RESET} Adds JUnit 5 and Mockk for testing.")
    println("  ${Colors.BOLD}GitHub Workflows:${Colors.RESET} Adds GitHub Actions workflows for CI/CD.")
    println("  ${Colors.BOLD}Back:${Colors.RESET} Return to the main menu.")
    println()
    readInput("Press Enter to continue...", allowBlank = true)
}

private fun showProjectVariablesHelp() {
    println()
    println(title("=== Project Variables Help ==="))
    println("  ${Colors.BOLD}Rename project:${Colors.RESET} Change the name of the project.")
    println("  ${Colors.BOLD}Set group:${Colors.RESET} Set the group ID for the project (e.g., com.example).")
    println("  ${Colors.BOLD}Back:${Colors.RESET} Return to the main menu.")
    println()
    readInput("Press Enter to continue...", allowBlank = true)
}
