package pt.rafap.kpb.utils

/**
 * Represents a configuration with key-value pairs.
 * @property map A map containing configuration key-value pairs.
 */
interface Config {
    val map: Map<String, String>

    /**
     * Returns the default configuration entries to be written to the config file.
     *
     * @return A map of configuration keys to their default values.
     */
    fun getDefaultConfigFileEntries(): Map<String, String>
}