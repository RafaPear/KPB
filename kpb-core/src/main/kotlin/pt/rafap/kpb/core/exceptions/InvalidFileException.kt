package pt.rafap.kpb.core.exceptions

/**
 * Exception thrown when a file operation fails due to invalid or corrupted file content.
 *
 * @param message Detailed error message explaining why the file is invalid.
 * @param type The severity level of the error.
 */
class InvalidFileException(
    message: String = "The provided file is invalid or corrupted",
    type: ErrorType
) : ReversiException(message, type)
