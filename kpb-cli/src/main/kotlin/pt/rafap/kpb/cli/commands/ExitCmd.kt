package pt.rafap.kpb.cli.commands

import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandInfo
import pt.rafap.ktflag.cmd.CommandResult
import kotlin.system.exitProcess

/**
 * Command to exit the application.
 */
object ExitCmd : CommandImpl<Any>() {
    override val info = CommandInfo(
        title = "Exit",
        description = "Exits the application.",
        aliases = listOf("exit", "quit", "q"),
        usage = "exit",
        minArgs = 0,
        maxArgs = 0
    )

    fun askNameToSave(): String? {
        print("Enter a name to save the current game (or leave empty to not save): ")
        val input = readln()
        return input.ifBlank { null }
    }

    override fun execute(vararg args: String, context: Any?): CommandResult<Any> {
        println("By byyyy")
        exitProcess(0)
    }
}