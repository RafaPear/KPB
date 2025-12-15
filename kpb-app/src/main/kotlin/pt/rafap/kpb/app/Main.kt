package pt.rafap.kpb.app

import androidx.compose.ui.window.*
import kpb.kpb_app.generated.resources.Res
import kpb.kpb_app.generated.resources.reversi
import org.jetbrains.compose.resources.painterResource
import pt.rafap.kpb.utils.LOGGER
import java.lang.System.setProperty

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
            icon = painterResource(Res.drawable.reversi),
            state = windowState,
        ) {
            window.minimumSize = java.awt.Dimension(800, 600)
        }
    }
}