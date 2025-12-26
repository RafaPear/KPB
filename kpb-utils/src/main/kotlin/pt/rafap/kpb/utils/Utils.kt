package pt.rafap.kpb.utils

import java.io.File

/**
 * Loads a resource file from the classpath.
 * @param path The path to the resource file.
 * @return The resource file.
 * @throws IllegalArgumentException if the resource file is not found.
 * @throws java.net.URISyntaxException if the resource URI is invalid.
 */
fun loadResource(path: String): File {
    val input = Thread.currentThread()
        .contextClassLoader
        .getResourceAsStream(path)
        ?: throw IllegalArgumentException("Resource '$path' not found")

    val temp = kotlin.io.path.createTempFile(
        prefix = "kpb-resource-",
        suffix = "-" + path.substringAfterLast('/')
    ).toFile()

    temp.outputStream().use { out ->
        input.copyTo(out)
    }

    temp.deleteOnExit()
    return temp
}
