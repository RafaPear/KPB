package pt.rafap.kpb.core.storage.serializers

import pt.rafap.kpb.core.exceptions.ErrorType
import pt.rafap.kpb.core.exceptions.InvalidFileException
import pt.rafap.kpb.core.storage.CoreState
import pt.rafap.kpb.storage.Serializer

/**
 * Serializer for the [CoreState] class, converting it to and from a String representation.
 *
 * This serializer is used by the storage backend to persist the core state to a file.
 * It handles serialization by converting the state ID to a string, and deserialization
 * by parsing the string back to an integer ID.
 */
internal class CoreStateSerializer : Serializer<CoreState, String> {

    /**
     * Serializes a [CoreState] object into a string.
     *
     * @param obj The [CoreState] object to serialize.
     * @return The string representation of the state (currently just the ID).
     */
    override fun serialize(obj: CoreState): String {
        return obj.id.toString()
    }

    /**
     * Deserializes a string into a [CoreState] object.
     *
     * @param obj The string representation of the state.
     * @return The deserialized [CoreState] object.
     * @throws InvalidFileException If the string cannot be parsed into a valid state ID.
     */
    override fun deserialize(obj: String): CoreState {
        val id = obj.toIntOrNull()
            ?: throw InvalidFileException(
                "Invalid CoreState id in file: '$obj'",
                ErrorType.ERROR
            )

        return CoreState(id)
    }
}