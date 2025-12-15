package pt.rafap.kpb.cli

import pt.rafap.ktflag.CommandParser
import pt.rafap.ktflag.ParserConfig
import pt.rafap.ktflag.cmd.CommandImpl
import pt.rafap.ktflag.cmd.CommandResultType
import pt.rafap.ktflag.style.Colors.colorText

/**
 * Simple command-line interface coordinator.
 *
 * Holds the set of available `CommandImpl<Game>` objects, manages an optional debug mode
 * and runs the interactive read-eval-print loop that dispatches commands to the parser.
 */
class CLI<T>(
    val commands: Array<CommandImpl<T>>,
    val debug: Boolean = false,
    debugCommands: Array<CommandImpl<T>> = arrayOf(),
) {
    private fun log(message: String) {
        println(colorText(message))
    }

    val finalCommands = commands + if (debug) debugCommands else arrayOf()

    val parserConfig = ParserConfig<T>(
        prompt = PROMPT,
        promptColor = PROMPT_COLOR,
        errorColor = ERROR_COLOR,
        warningColor = WARNING_COLOR,
        infoColor = INFO_COLOR,
        helpUsageColor = HELP_USAGE_COLOR,
        helpAliasColor = HELP_ALIAS_COLOR,
        helpDescColor = HELP_DESC_COLOR,
    )

    val parser = CommandParser(*finalCommands, config = parserConfig)

    /**
     * Entry point for the CLI version of the Reversi context.
     * Initializes the board and command parser, and handles user input.
     */
    fun startLoop() {
        /**
         * The current context.
         */
        var context: T? = null

        println(colorText(WELCOME_MESSAGE, TEXT_COLOR))
        while (true) {
            val input = parser.readInput()
            context = parseInput(input, context)
        }
    }

    fun parseInput(input: String, context: T? = null): T? {
        var context: T? = context

        val result = parser.parseInputToResult(input, context)

        if (result == null) {
            println(colorText("[ERROR] Unknown command", ERROR_COLOR))
            return context
        }

        when {
            result.type == CommandResultType.UNKNOWN_COMMAND  -> parser.printUnknownCommandError(input, result)

            result.type != CommandResultType.SUCCESS          -> result.printError(parserConfig)

            result.type == CommandResultType.SUCCESS && debug -> {
                log(result.message); context = result.result
            }

            result.result != null                             -> context = result.result
        }

        return context
    }
}
