package pt.rafap.kpb.core.exceptions

/**
 * Base exception class for Reversi game-related errors.
 *
 * All custom exceptions in the core module should extend this class to ensure
 * consistent error handling and reporting with an associated [ErrorType].
 *
 * @property type The type of error represented by this exception.
 * @param message The detail message for the exception.
 */
abstract class ReversiException(
    message: String,
    val type: ErrorType
) : Exception(message)