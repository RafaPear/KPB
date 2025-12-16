package pt.rafap.kpb.core.exceptions

/**
 * Enum representing different types of error levels for exceptions in the Reversi game.
 *
 * @property level The string representation of the error level.
 */
@Suppress("unused")
enum class ErrorType(val level: String) {
    /** Informational message, no error occurred. */
    INFO("INFO"),
    /** Warning condition, operation may have partially succeeded or failed non-critically. */
    WARNING("WARNING"),
    /** Standard error, operation failed. */
    ERROR("ERROR"),
    /** Critical error, application state may be compromised. */
    CRITICAL("CRITICAL");

    companion object {
        /**
         * Converts a standard [Exception] into a [ReversiException] with a specified error type.
         *
         * @param type The [ErrorType] to assign to the new exception.
         * @return A new [ReversiException] wrapping the original exception's message.
         */
        @Suppress("unused")
        fun Exception.toReversiException(type: ErrorType): ReversiException {
            return object : ReversiException(
                message = message ?: "An unknown error occurred.",
                type = type
            ) {}
        }
    }
}