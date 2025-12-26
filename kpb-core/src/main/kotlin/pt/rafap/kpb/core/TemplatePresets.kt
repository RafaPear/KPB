package pt.rafap.kpb.core

/**
 * Pre-built template presets for common module types.
 * These help developers quickly scaffold standard module structures.
 */
object TemplatePresets {

    /**
     * Main.kt for a Compose Desktop application
     */
    fun composeDesktopMainKt(moduleGroup: String, moduleName: String, projectName: String): String = """
        package $moduleGroup
        
        import androidx.compose.foundation.layout.Column
        import androidx.compose.foundation.layout.Row
        import androidx.compose.foundation.layout.fillMaxWidth
        import androidx.compose.material.AlertDialog
        import androidx.compose.material.Button
        import androidx.compose.material.MaterialTheme
        import androidx.compose.material.OutlinedTextField
        import androidx.compose.material.Surface
        import androidx.compose.material.Text
        import androidx.compose.material.TopAppBar
        import androidx.compose.runtime.Composable
        import androidx.compose.runtime.getValue
        import androidx.compose.runtime.mutableStateOf
        import androidx.compose.runtime.remember
        import androidx.compose.runtime.setValue
        import androidx.compose.ui.Modifier
        import androidx.compose.ui.window.Window
        import androidx.compose.ui.window.application
        
        fun main() = application {
            Window(onCloseRequest = ::exitApplication, title = "$projectName - $moduleName") {
                MaterialTheme {
                    Surface {
                        Column {
                            TopAppBar(
                                title = { Text("Welcome to $moduleName") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                App(onClose = ::exitApplication)
                            }
                        }
                    }
                }
            }
        }
        
        @Composable
        fun App(onClose: () -> Unit) {
            var text by remember { mutableStateOf("") }
            var showDialog by remember { mutableStateOf(false) }
        
            MaterialTheme {
                Column {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        label = { Text("Mensagem") }
                    )
        
                    Button(onClick = { showDialog = true }) {
                        Text("Mostrar diálogo")
                    }
        
                    Button(onClick = onClose) {
                        Text("Fechar")
                    }
                }
        
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        confirmButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("OK")
                            }
                        },
                        title = { Text("Mensagem") },
                        text = { Text(text) }
                    )
                }
            }
        }
    """.trimIndent()

    /**
     * Main.kt for a CLI application
     */
    fun cliMainKt(moduleGroup: String, moduleName: String, projectName: String): String = $$"""
        package $$moduleGroup
        
        fun main(args: Array<String>) {
            println("╔════════════════════════════════════╗")
            println("║  Welcome to $$projectName - $$moduleName  ║")
            println("╚════════════════════════════════════╝")
            println()
            
            if (args.isEmpty()) {
                printUsage()
            } else {
                handleCommand(args)
            }
        }
        
        private fun printUsage() {
            println("Usage: $$moduleName [command] [options]")
            println()
            println("Commands:")
            println("  help    - Show this help message")
            println("  version - Show version information")
        }
        
        private fun handleCommand(args: Array<String>) {
            when (args[0]) {
                "help" -> printUsage()
                "version" -> println("$$moduleName v1.0.0")
                else -> println("Unknown command: ${args[0]}")
            }
        }
    """.trimIndent()

    /**
     * Main.kt for a library/API module
     */
    fun libraryMainKt(moduleGroup: String, moduleName: String): String = """
        package $moduleGroup
        
        /**
         * Main API interface for the $moduleName module.
         */
        interface ${moduleName.split("-").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }}Api {
            /**
             * Initialize the module.
             */
            fun initialize()
            
            /**
             * Cleanup resources.
             */
            fun cleanup()
        }
        
        /**
         * Default implementation of the $moduleName API.
         */
        class ${
        moduleName.split("-").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }
    }ApiImpl : ${moduleName.split("-").joinToString("") { it.replaceFirstChar { c -> c.uppercaseChar() } }}Api {
            override fun initialize() {
                println("Initializing $moduleName...")
            }
            
            override fun cleanup() {
                println("Cleaning up $moduleName...")
            }
        }
    """.trimIndent()

    /**
     * README.md for a standard module
     */
    fun moduleReadme(moduleName: String, projectName: String): String = """
        # $moduleName Module
        
        Part of the $projectName project.
        
        ## Overview
        
        This module provides...
        
        ## Features
        
        - Feature 1
        - Feature 2
        - Feature 3
        
        ## Usage
        
        ```kotlin
        // Example usage here
        ```
        
        ## Requirements
        
        - Kotlin 1.8+
        - Java 11+
        
        ## License
        
        See main project LICENSE file.
    """.trimIndent()

    /**
     * .gitignore for Kotlin projects
     */
    fun gitignore(): String = """
        # Gradle
        .gradle/
        build/
        gradle-app.setting
        !gradle-wrapper.jar
        .gradletasknamecache
        
        # IDE
        .idea/
        .vscode/
        *.iml
        *.iws
        *.ipr
        
        # OS
        .DS_Store
        .DS_Store?
        ._*
        .Spotlight-V100
        .Trashes
        ehthumbs.db
        Thumbs.db
        
        # Build artifacts
        out/
        *.class
        *.jar
        *.war
        *.ear
        *.zip
        *.tar
        *.tar.gz
        
        # Local properties
        local.properties
    """.trimIndent()
}

