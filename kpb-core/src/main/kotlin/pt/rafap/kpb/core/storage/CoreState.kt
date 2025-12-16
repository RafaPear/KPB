package pt.rafap.kpb.core.storage

/**
 * Represents the state of the core.
 *
 * This class encapsulates the minimal state required to persist the core module's data.
 * Currently, it only holds an ID, but it can be extended to include more complex state.
 *
 * @property id The unique identifier for the state. Must be non-negative.
 */
data class CoreState(val id: Int) {
    init {
        assert(id >= 0) { "CoreState id must be non-negative" }
    }
}
