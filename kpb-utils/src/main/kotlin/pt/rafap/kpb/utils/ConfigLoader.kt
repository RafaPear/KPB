package pt.rafap.kpb.utils

import java.io.File
import java.util.*

/**
 * Configuration loader.
 * Loads configuration from a properties file and validates expected entries.
 */
class ConfigLoader<U : Config>(
    val path: String,
    val factory: (Map<String, String>) -> U,
) {
    /**
     * Loads the configuration from the properties file.
     *
     * If the file doesn't exist, creates it with default values.
     * Merges any new default entries into the existing file.
     *
     * @return The loaded configuration instance.
     */
    fun loadConfig(): U {
        val props = Properties()
        val file = File(path)
        val defaultEntries = factory(emptyMap()).getDefaultConfigFileEntries()

        if (!file.exists()) {
            file.parentFile?.mkdirs()
            file.createNewFile()
            val entries = defaultEntries
            for (entry in entries)
                props.setProperty(entry.key, entry.value)

            file.outputStream().use { output ->
                props.store(output, "Configuration file created at ${file.absolutePath}")
            }
        }

        file.inputStream().use { input ->
            props.load(input)
        }

        val configMap = props.entries.associate { it.key.toString() to it.value.toString() }
        val factoryResult = factory(configMap)
        val newProps = Properties().also {
            it.putAll(factoryResult.getDefaultConfigFileEntries())
        }

        file.outputStream().use { output ->
            newProps.store(output, "Configuration file created at ${file.absolutePath}")
        }
        return factoryResult
    }
}