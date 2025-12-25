package pt.rafap.kpb.app

import androidx.compose.ui.window.*
import pt.rafap.kpb.utils.LOGGER
import java.lang.System.setProperty

/**
 * Main entry point for the KPB application.
 *
 * Initializes a Compose Desktop window for the interactive project builder interface.
 *
 * @param args Command-line arguments (currently unused).
 */
fun main(args: Array<String>) {
    setProperty("apple.awt.application.name", "kbp-DEV")
    
    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault
        )

        fun safeExitApplication() {
            LOGGER.info("Exiting application...")
            exitApplication()
        }

        Window(
            onCloseRequest = ::safeExitApplication,
            title = "kbp-DEV",
            state = windowState,
        ) {
            window.minimumSize = java.awt.Dimension(800, 600)
        }
    }
}