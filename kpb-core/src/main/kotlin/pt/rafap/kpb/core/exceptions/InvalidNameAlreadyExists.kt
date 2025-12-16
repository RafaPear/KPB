package pt.rafap.kpb.core.exceptions

/**
 * Exception thrown when attempting to create an entity with a name that is already in use.
 *
 * @param message Detailed error message.
 * @param type The severity level of the error, defaults to WARNING.
 */
@Suppress("unused")
class InvalidNameAlreadyExists(
    message: String = "The provided name already exists",
    type: ErrorType = ErrorType.WARNING
) : ReversiException(message, type)
