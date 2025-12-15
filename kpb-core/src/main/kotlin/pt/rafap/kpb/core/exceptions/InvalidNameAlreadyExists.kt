package pt.rafap.kpb.core.exceptions

class InvalidNameAlreadyExists(
    message: String = "The provided name already exists",
    type: ErrorType = ErrorType.WARNING
) : ReversiException(message, type)
