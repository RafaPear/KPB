#Module kpb-storage

Storage abstractions for synchronous and asynchronous operations with file-backed implementations.

#Package pt.rafap.kpb.storage
Storage interfaces and file-based implementations.

## Usage
Async file storage for a domain type:

```kotlin
data class User(val id: Int, val name: String)
class UserSerializer : Serializer<User, String> {
    override fun serialize(obj: User) = "${obj.id},${obj.name}"
    override fun deserialize(obj: String) = obj.split(",").let { User(it[0].toInt(), it[1]) }
}
val storage = AsyncFileStorage("users", UserSerializer())
// use within coroutines
```
