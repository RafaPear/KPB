package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.project.KpbFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ModuleTest {

    @Test
    fun `plus operator merges modules correctly`() {
        val m1 = Module(
            name = "mod",
            simpleName = "mod",
            group = "g",
            files = listOf(KpbFile("f1", "c1")),
            gradleFiles = emptyList(),
            versionCatalog = VersionCatalog()
        )
        val m2 = Module(
            name = "mod",
            simpleName = "mod",
            group = "g",
            files = listOf(KpbFile("f2", "c2")),
            gradleFiles = emptyList(),
            versionCatalog = VersionCatalog()
        )

        val merged = m1 + m2
        assertEquals(2, merged.files.size)
        assertTrue(merged.files.any { it.path == "f1" })
        assertTrue(merged.files.any { it.path == "f2" })
    }

    @Test
    fun `plus operator fails for different modules`() {
        val m1 = Module(
            name = "mod1",
            simpleName = "mod1",
            group = "g",
            files = emptyList(),
            gradleFiles = emptyList(),
            versionCatalog = VersionCatalog()
        )
        val m2 = Module(
            name = "mod2",
            simpleName = "mod2",
            group = "g",
            files = emptyList(),
            gradleFiles = emptyList(),
            versionCatalog = VersionCatalog()
        )

        assertFailsWith<IllegalArgumentException> {
            m1 + m2
        }
    }

    @Test
    fun `buildModule creates module correctly`() {
        val module = Module.buildModule("my.mod", "mod", "my") {
            fileModule("test.txt") { "hello" }
        }

        assertEquals("my.mod", module.name)
        assertEquals("mod", module.simpleName)
        assertEquals("my.mod", module.group)
        assertEquals(1, module.files.size)
        assertEquals("test.txt", module.files.first().path)
        assertEquals("hello", module.files.first().content)
    }

    @Test
    fun `buildModule handles null group`() {
        val module = Module.buildModule("mod", "mod", null) {
            // empty
        }
        assertEquals("mod", module.group)
    }
}

