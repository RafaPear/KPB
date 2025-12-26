package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Lib
import pt.rafap.kpb.core.gradle.content.Plugin
import pt.rafap.kpb.core.gradle.content.Version
import kotlin.test.*

class VersionCatalogTest {

    @Test
    fun `verifyIntegrity fails on duplicate plugin IDs`() {
        val p1 = Plugin("p1", "id1", "v1")
        val p2 = Plugin("p2", "id1", "v2") // Duplicate ID

        assertFailsWith<IllegalArgumentException> {
            VersionCatalog(plugins = listOf(p1, p2))
        }
    }

    @Test
    fun `verifyIntegrity fails on duplicate lib names`() {
        val l1 = Lib("lib1", "g:a", "1.0")
        val l2 = Lib("lib1", "g:b", "2.0") // Duplicate name

        assertFailsWith<IllegalArgumentException> {
            VersionCatalog(libs = listOf(l1, l2))
        }
    }

    @Test
    fun `verifyIntegrity fails on duplicate version names`() {
        val v1 = Version("v1", "1.0")
        val v2 = Version("v1", "2.0") // Duplicate name

        assertFailsWith<IllegalArgumentException> {
            VersionCatalog(versions = listOf(v1, v2))
        }
    }

    @Test
    fun `toKbpFile generates correct TOML content`() {
        val v = Version("kotlin", "1.9.0")
        val l = Lib("stdlib", "org.jetbrains.kotlin:kotlin-stdlib", "kotlin")
        val p = Plugin("kotlin-jvm", "org.jetbrains.kotlin.jvm", "kotlin")

        val catalog = VersionCatalog(
            versions = listOf(v),
            libs = listOf(l),
            plugins = listOf(p)
        )

        val kpbFile = catalog.toKbpFile()
        assertEquals("gradle/libs.versions.toml", kpbFile.path)

        val content = kpbFile.content
        assertNotNull(content)
        assertTrue(content.contains("[versions]"))
        assertTrue(content.contains("kotlin = \"1.9.0\""))

        assertTrue(content.contains("[libraries]"))
        assertTrue(content.contains("stdlib = { module = \"org.jetbrains.kotlin:kotlin-stdlib\", version.ref = \"kotlin\" }"))

        assertTrue(content.contains("[plugins]"))
        assertTrue(content.contains("kotlin-jvm = { id = \"org.jetbrains.kotlin.jvm\", version.ref = \"kotlin\" }"))
    }

    @Test
    fun `plus operator merges catalogs correctly`() {
        val v1 = Version("v1", "1.0")
        val v2 = Version("v2", "2.0")
        val c1 = VersionCatalog(versions = listOf(v1))
        val c2 = VersionCatalog(versions = listOf(v2))

        val merged = c1 + c2
        assertEquals(2, merged.versions.size)
        assertTrue(merged.versions.any { it.name == "v1" })
        assertTrue(merged.versions.any { it.name == "v2" })
    }

    @Test
    fun `plus operator handles duplicates by keeping first`() {
        val v1 = Version("v1", "1.0")
        val v2 = Version("v1", "2.0") // Same name, different version
        val c1 = VersionCatalog(versions = listOf(v1))
        val c2 = VersionCatalog(versions = listOf(v2))

        val merged = c1 + c2
        assertEquals(1, merged.versions.size)
        assertEquals("1.0", merged.versions.first().version)
    }
}

