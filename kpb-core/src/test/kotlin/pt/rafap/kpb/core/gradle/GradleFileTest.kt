package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.Lib
import pt.rafap.kpb.core.gradle.content.Plugin
import kotlin.test.*

class GradleFileTest {

    @Test
    fun `GradleFile creation fails if plugin not in catalog`() {
        val plugin = Plugin("plugin-alias", "plugin.id", "version-alias")
        val catalog = VersionCatalog() // Empty catalog

        assertFailsWith<IllegalArgumentException> {
            GradleFile(
                name = "build.gradle.kts",
                imports = emptyList(),
                plugins = listOf(plugin),
                otherPlugins = emptyList(),
                dependencies = emptyList(),
                libs = emptyList(),
                modules = emptyList(),
                others = emptyList(),
                repositories = emptyList(),
                versionCatalog = catalog
            )
        }
    }

    @Test
    fun `GradleFile creation fails if lib not in catalog`() {
        val lib = Lib("my-lib", "group:artifact", "1.0.0")
        val catalog = VersionCatalog() // Empty catalog

        assertFailsWith<IllegalArgumentException> {
            GradleFile(
                name = "build.gradle.kts",
                imports = emptyList(),
                plugins = emptyList(),
                otherPlugins = emptyList(),
                dependencies = emptyList(),
                libs = listOf(lib),
                modules = emptyList(),
                others = emptyList(),
                repositories = emptyList(),
                versionCatalog = catalog
            )
        }
    }

    @Test
    fun `toKbpFile generates correct content`() {
        val plugin = Plugin("plugin-alias", "plugin.id", "version-alias")
        val lib = Lib("my-lib", "group:artifact", "1.0.0")
        val catalog = VersionCatalog(
            plugins = listOf(plugin),
            libs = listOf(lib)
        )

        val gradleFile = GradleFile(
            name = "build.gradle.kts",
            imports = listOf("java.util.*"),
            plugins = listOf(plugin),
            otherPlugins = emptyList(),
            dependencies = emptyList(),
            libs = listOf(lib),
            modules = emptyList(),
            others = emptyList(),
            repositories = emptyList(),
            versionCatalog = catalog
        )

        val kpbFile = gradleFile.toKbpFile()
        assertEquals("build.gradle.kts", kpbFile.path)
        val content = kpbFile.content
        assertNotNull(content)
        assertTrue(content.contains("import java.util.*"))
        assertTrue(content.contains("alias(libs.plugins.plugin.alias)"))
        assertTrue(content.contains("implementation(libs.my-lib)"))
    }

    @Test
    fun `plus operator merges content correctly`() {
        val plugin1 = Plugin("p1", "id1", "v1")
        val plugin2 = Plugin("p2", "id2", "v1")
        val catalog1 = VersionCatalog(plugins = listOf(plugin1))
        val catalog2 = VersionCatalog(plugins = listOf(plugin2))

        val gf1 = GradleFile(
            name = "build.gradle.kts",
            imports = listOf("i1"),
            plugins = listOf(plugin1),
            otherPlugins = emptyList(),
            dependencies = emptyList(),
            libs = emptyList(),
            modules = emptyList(),
            others = emptyList(),
            repositories = emptyList(),
            versionCatalog = catalog1
        )

        val gf2 = GradleFile(
            name = "build.gradle.kts",
            imports = listOf("i2"),
            plugins = listOf(plugin2),
            otherPlugins = emptyList(),
            dependencies = emptyList(),
            libs = emptyList(),
            modules = emptyList(),
            others = emptyList(),
            repositories = emptyList(),
            versionCatalog = catalog2
        )

        val merged = gf1 + gf2
        assertEquals(2, merged.imports.size)
        assertTrue(merged.imports.contains("i1"))
        assertTrue(merged.imports.contains("i2"))
        assertEquals(2, merged.plugins.size)
        assertEquals(2, merged.versionCatalog.plugins.size)
    }
}
