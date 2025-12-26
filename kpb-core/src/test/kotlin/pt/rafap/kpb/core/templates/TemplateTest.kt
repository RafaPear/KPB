package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.project.KpbFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TemplateTest {

    @Test
    fun `plus operator merges templates correctly`() {
        val t1 = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kpbFiles = listOf(KpbFile("f1", "c1")),
            handlers = emptyList()
        )
        val t2 = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kpbFiles = listOf(KpbFile("f2", "c2")),
            handlers = emptyList()
        )

        val merged = t1 + t2
        assertEquals(2, merged.kpbFiles.size)
        assertTrue(merged.kpbFiles.any { it.path == "f1" })
        assertTrue(merged.kpbFiles.any { it.path == "f2" })
    }

    @Test
    fun `addAllTemplates merges multiple templates`() {
        val t1 = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kpbFiles = listOf(KpbFile("f1", "c1")),
            handlers = emptyList()
        )
        val t2 = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kpbFiles = listOf(KpbFile("f2", "c2")),
            handlers = emptyList()
        )
        val t3 = Template(
            versionCatalog = VersionCatalog(),
            modules = emptyList(),
            gradleFiles = emptyList(),
            kpbFiles = listOf(KpbFile("f3", "c3")),
            handlers = emptyList()
        )

        val merged = Template.addAllTemplates(t1, t2, t3)
        assertEquals(3, merged.kpbFiles.size)
    }

    @Test
    fun `EMPTY_TEMPLATE is truly empty`() {
        val empty = Template.EMPTY_TEMPLATE
        assertTrue(empty.versionCatalog.libs.isEmpty())
        assertTrue(empty.versionCatalog.plugins.isEmpty())
        assertTrue(empty.versionCatalog.versions.isEmpty())
        assertTrue(empty.modules.isEmpty())
        assertTrue(empty.gradleFiles.isEmpty())
        assertTrue(empty.kpbFiles.isEmpty())
        assertTrue(empty.handlers.isEmpty())
    }
}

