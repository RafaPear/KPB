package pt.rafap.kpb.core.storage

import pt.rafap.kpb.core.storage.serializers.CoreStateSerializer
import pt.rafap.kpb.storage.AsyncFileStorage
import pt.rafap.kpb.storage.AsyncStorage

enum class GameStorageType(val storage: (String) -> AsyncStorage<String, CoreState, String>) {
    FILE_STORAGE({ folder ->
                     AsyncFileStorage(
                         folder = folder,
                         serializer = CoreStateSerializer()
                     )
                 });

    companion object {
        fun fromConfigValue(value: String): GameStorageType =
            entries.firstOrNull { it.name == value } ?: FILE_STORAGE
    }
}