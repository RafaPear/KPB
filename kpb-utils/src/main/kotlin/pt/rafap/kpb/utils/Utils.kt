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
    val classloader = Thread.currentThread().getContextClassLoader()
    val resource = classloader.getResource(path)
        ?: throw IllegalArgumentException("Resource '$path' not found")
    return File(resource.toURI())
}