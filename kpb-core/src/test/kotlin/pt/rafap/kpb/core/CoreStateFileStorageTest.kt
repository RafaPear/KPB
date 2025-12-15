package pt.rafap.kpb.core

import kotlinx.coroutines.runBlocking
import pt.rafap.kpb.core.storage.CoreState
import pt.rafap.kpb.core.storage.GameStorageType
import pt.rafap.kpb.utils.CONFIG_FOLDER
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFails

class CoreStateFileStorageTest {
    val storage = GameStorageType.FILE_STORAGE.storage("test-saves")

    val defaultCoreState = CoreState(1)

    fun cleanup(func: suspend () -> Unit) {
        File("test-saves").deleteRecursively()
        File(CONFIG_FOLDER).deleteRecursively()
        runBlocking { func() }
        File(CONFIG_FOLDER).deleteRecursively()
        File("test-saves").deleteRecursively()
    }

    @Test
    fun `Run new at an already existing id fails`() {
        cleanup {
            assertFails {
                storage.new(1.toString()) {
                    defaultCoreState
                }
                storage.new(1.toString()) {
                    defaultCoreState
                }
            }
        }
    }

    @Test
    fun `Run save at a non existing id fails`() {
        cleanup {
            assertFails {
                storage.save(1.toString(), defaultCoreState)
            }
        }
    }

    @Test
    fun `Run load at a non existing id returns null`() {
        cleanup {
            val gs = storage.load(1.toString())
            assert(gs == null)
        }
    }

    @Test
    fun `Run new and load works`() {
        cleanup {
            val gs1 = storage.new(1.toString()) { defaultCoreState }
            val gs2 = storage.load(1.toString())

            assert(gs1 == gs2)
        }
    }

    @Test
    fun `Run new, save and load works`() {
        cleanup {
            val gs1 = storage.new(1.toString()) {
                defaultCoreState
            }.copy(id = 2)

            storage.save(1.toString(), gs1)

            val gs2 = storage.load(1.toString())

            assert(gs1 == gs2)
        }
    }

    @Test
    fun `Run new, delete and load returns null`() {
        cleanup {
            storage.new(1.toString()) { defaultCoreState }
            storage.delete(1.toString())
            val gs = storage.load(1.toString())

            assert(gs == null)
        }
    }

    @Test
    fun `Run delete at a non existing id fails`() {
        cleanup {
            assertFails {
                storage.delete(1.toString())
            }
        }
    }
}