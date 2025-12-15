package pt.rafap.kpb.core.project

import java.io.File

class KbpFile(
    val path: String,
    val content: String?
) {

    fun create() {
        val file = File(path)
        file.parentFile?.mkdirs()
        file.writeText(content ?: "")
    }
}
