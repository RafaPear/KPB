package pt.rafap.kpb.core.storage

/**
 * Represents the state of the core.
 * @property id The unique identifier for the state.
 */
data class CoreState(val id: Int) {
    init {
        assert(id >= 0) { "CoreState id must be non-negative" }
    }
}

