package pt.rafap.kpb.utils

import java.util.logging.ConsoleHandler
import java.util.logging.Logger

const val BASE_FOLDER = "data"

const val CONFIG_FOLDER = "$BASE_FOLDER/config"
const val CORE_CONFIG_FILE = "$CONFIG_FOLDER/kbp-core.properties"
const val CLI_CONFIG_FILE = "$CONFIG_FOLDER/kbp-cli.properties"
const val APP_CONFIG_FILE = "$CONFIG_FOLDER/kbp-app.properties"

val BASE_LOG_FILE_NAME = makePathString("logs/kbp-app")

val LOGGER: Logger = Logger.getGlobal().also {
    val consoleHandler = ConsoleHandler().also { handler ->
        handler.formatter = PlainFormatter()
    }
    it.addHandler(consoleHandler)
    it.useParentHandlers = false
}
