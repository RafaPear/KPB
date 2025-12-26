package pt.rafap.kpb.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import pt.rafap.kpb.utils.LogManager
import java.lang.System.setProperty

private val logger = LogManager.getLogger("App")

/**
 * Main entry point for the KPB application.
 *
 * Initializes a Compose Desktop window for the interactive project builder interface.
 */
fun main() {
    setProperty("apple.awt.application.name", "kbp-DEV")
    // ensure file logging is configured alongside console
    LogManager.init(isCli = false)
    logger.info("Starting KPB App")

    application {
        val windowState = rememberWindowState(
            placement = WindowPlacement.Floating,
            position = WindowPosition.PlatformDefault
        )

        fun safeExitApplication() {
            logger.info("Exiting application...")
            exitApplication()
        }

        Window(
            onCloseRequest = ::safeExitApplication,
            title = "kbp-DEV",
            state = windowState,
        ) {
            window.minimumSize = java.awt.Dimension(800, 600)

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)
            ) {
                Text("Audio Test")
                Button(onClick = {
                    logger.info("Play BGM clicked")
                    Audio.playBGM()
                }) { Text("Play BGM") }
                Button(onClick = {
                    logger.info("Stop BGM clicked")
                    Audio.stopBGM()
                }) { Text("Stop BGM") }
                Button(onClick = {
                    logger.info("Play Hit clicked")
                    Audio.playHit()
                }) { Text("Play Hit") }
                Button(onClick = {
                    logger.info("Play Piece clicked")
                    Audio.playPiece()
                }) { Text("Play Piece") }
                Button(onClick = {
                    logger.info("Play MEGALOVANIA clicked")
                    Audio.playMega()
                }) { Text("Play MEGALOVANIA") }
                Button(onClick = {
                    logger.info("Stop All clicked")
                    Audio.stopAll()
                }) { Text("Stop All") }
            }
        }
    }
}