package pt.rafap.kpb.utils

import java.io.File
import java.time.LocalDate
import java.util.logging.ConsoleHandler
import java.util.logging.FileHandler
import java.util.logging.Level
import java.util.logging.Logger

object LogManager {
    private val fileHandler: FileHandler by lazy {
        val date = LocalDate.now()
        var name = "${BASE_LOG_FILE_NAME}-$date.log"
        var count = 1
        while (File(name).exists()) {
            name = "${BASE_LOG_FILE_NAME}-$date-${count}.log"
            count++
        }
        File(name).parentFile?.mkdirs()
        File(name).createNewFile()
        FileHandler(name, true).apply {
            formatter = PlainFormatter()
            level = Level.ALL
        }
    }

    fun init(isCli: Boolean) {
        val rootLogger = Logger.getLogger("")
        rootLogger.level = Level.INFO

        // Enable detailed logging only for our application
        val appLogger = Logger.getLogger("pt.rafap.kpb")
        appLogger.level = Level.FINE

        // Remove existing handlers to avoid duplicates
        rootLogger.handlers.forEach { rootLogger.removeHandler(it) }

        if (!isCli) {
            val consoleHandler = ConsoleHandler()
            consoleHandler.formatter = PlainFormatter()
            consoleHandler.level = Level.ALL
            rootLogger.addHandler(consoleHandler)
        }

        // Always log to file as requested for CLI, and usually good for App too.
        // If user strictly meant "only logs to terminal if not cli", they might imply "logs to file if cli".
        // But "log in the background to the file" for CLI.
        // I'll add file handler for both for now, as it's safer to have logs.
        rootLogger.addHandler(fileHandler)
    }

    fun getLogger(name: String): Logger {
        return Logger.getLogger(name)
    }
}
