package pt.rafap.kpb.core.project

import java.io.File

/**
 * Represents a file to be created in the generated project.
 *
 * @property path The relative path of the file within the project structure.
 * @property content The text content of the file.
 */
class KpbFile(
    val path: String,
    val content: String?
) {

    /**
     * Writes the file to disk, creating parent directories if necessary.
     */
    fun create() {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content ?: "")
    }
}
