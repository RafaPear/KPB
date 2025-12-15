package pt.rafap.kpb.core

import pt.rafap.kpb.utils.CORE_CONFIG_FILE
import pt.rafap.kpb.utils.ConfigLoader

fun loadCoreConfig(): CoreConfig = ConfigLoader(CORE_CONFIG_FILE) {
    CoreConfig(it)
}.loadConfig()