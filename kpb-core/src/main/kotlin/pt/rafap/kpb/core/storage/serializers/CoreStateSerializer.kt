package pt.rafap.kpb.core.storage.serializers

import pt.rafap.kpb.core.exceptions.ErrorType
import pt.rafap.kpb.core.exceptions.InvalidFileException
import pt.rafap.kpb.core.storage.CoreState
import pt.rafap.kpb.storage.Serializer

/**
 * Serializer for the GameState class, converting it to and from a String representation.
 */
internal class CoreStateSerializer : Serializer<CoreState, String> {

    override fun serialize(obj: CoreState): String {
        return obj.id.toString()
    }

    override fun deserialize(obj: String): CoreState {
        val id = obj.toIntOrNull()
            ?: throw InvalidFileException(
                "Invalid CoreState id in file: '$obj'",
                ErrorType.ERROR
            )

        return CoreState(id)
    }
}