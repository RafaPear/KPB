package pt.rafap.kpb.core.exceptions

class InvalidFileException(
    message: String = "The provided file is invalid or corrupted",
    type: ErrorType
) : ReversiException(message, type)
