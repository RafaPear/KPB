package pt.rafap.kpb.core.storage

import pt.rafap.kpb.core.storage.serializers.CoreStateSerializer
import pt.rafap.kpb.storage.AsyncFileStorage
import pt.rafap.kpb.storage.AsyncStorage

/**
 * Enum representing the available storage backends for the game.
 *
 * @property storage A factory function that creates an [AsyncStorage] instance given a folder path.
 */
enum class GameStorageType(val storage: (String) -> AsyncStorage<String, CoreState, String>) {
    /**
     * File-based storage implementation.
     * Stores game states as files in a specified directory.
     */
    FILE_STORAGE({ folder ->
                     AsyncFileStorage(
                         folder = folder,
                         serializer = CoreStateSerializer()
                     )
                 });

    companion object {
        /**
         * Resolves a [GameStorageType] from a configuration string value.
         * Defaults to [FILE_STORAGE] if the value does not match any enum entry.
         */
        fun fromConfigValue(value: String): GameStorageType =
            entries.firstOrNull { it.name == value } ?: FILE_STORAGE
    }
}