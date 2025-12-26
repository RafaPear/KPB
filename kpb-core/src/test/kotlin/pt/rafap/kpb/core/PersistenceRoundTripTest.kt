package pt.rafap.kpb.core

import java.io.File
import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PersistenceRoundTripTest {

    @Test
    fun `folder save and load preserves module files`() {
        val tempDir = createTempDirectory(prefix = "kpb-persist-").toFile()
        try {
            // Build a small project with one module and a main file
            val pm = ProjectManager.create("test")
            pm.setGroup("pt.test")
            pm.addModule("test-app", "app")
            val mainPath = "src/main/kotlin/pt/test/app/Main.kt"
            val mainContent = (
                    """
                package pt.test.app

                fun main() {
                    println("Hello from persistence test")
                }
                """.trimIndent() + "\n".repeat(5)
                    )
            pm.addSourceFile("test-app", mainPath, mainContent)

            // Save to folder
            pm.saveConfigurationFolder(tempDir.absolutePath)

            // Load back
            val pmLoaded = ProjectManager.loadConfigurationFolder(tempDir.absolutePath)
            val loadedModule = pmLoaded.project.modules.find { it.name == "test-app" }
            assertNotNull(loadedModule, "Module should be restored")

            val originalLen = mainContent.length
            val loadedFile = loadedModule.files.find { it.path == mainPath }
            assertNotNull(loadedFile, "Main file should be restored with same path")
            assertEquals(originalLen, loadedFile.content?.length, "Main file length should match after round-trip")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `folder save and load preserves module catalog`() {
        val tempDir = createTempDirectory(prefix = "kpb-catalog-").toFile()
        try {
            val pm = ProjectManager.create("cat").setGroup("pt.cat")
            pm.addModule("lib", "lib")
            pm.addLibrary(
                "lib",
                alias = "kotlin",
                id = "org.jetbrains.kotlin:kotlin-stdlib",
                versionName = "kotlin",
                versionValue = "1.9.21"
            )
            pm.saveConfigurationFolder(tempDir.absolutePath)

            val loaded = ProjectManager.loadConfigurationFolder(tempDir.absolutePath)
            val mod = loaded.project.modules.first { it.name == "lib" }
            val catalog = mod.versionCatalog
            assertEquals("kotlin", catalog.versions.firstOrNull()?.name)
            assertEquals("kotlin", catalog.libs.firstOrNull()?.name)
            assertEquals(1, catalog.libs.size)
            assertEquals(1, catalog.versions.size)
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `inline save and load preserves gradle metadata`() {
        val tempFile = kotlin.io.path.createTempFile(prefix = "kpb-inline-", suffix = ".json").toFile()
        try {
            val pm = ProjectManager.create("inline-test")
            pm.setGroup("pt.inline")
            pm.addModule("lib", "lib")
            val mainPath = "src/main/kotlin/pt/inline/lib/Lib.kt"
            pm.addSourceFile("lib", mainPath, "package pt.inline.lib\nclass Lib")
            pm.addLibrary(
                "lib",
                alias = "kotlin",
                id = "org.jetbrains.kotlin:kotlin-stdlib",
                versionName = "kotlin",
                versionValue = "1.9.0"
            )

            pm.saveConfiguration(tempFile.absolutePath)
            val loaded = ProjectManager.loadConfiguration(tempFile.absolutePath)
            val mod = loaded.project.modules.firstOrNull { it.name == "lib" }
            assertNotNull(mod, "Module should load")
            val gf = mod.gradleFiles.firstOrNull()
            assertNotNull(gf, "Gradle file should persist")
            assertEquals("kotlin", gf.libs.firstOrNull()?.name, "Library alias should persist")
            assertEquals("kotlin", gf.versionCatalog.versions.firstOrNull()?.name, "Version catalog should persist")
            val file = mod.files.firstOrNull { it.path == mainPath }
            assertNotNull(file, "Main file path should persist inline")
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `folder save respects new name and group on load`() {
        val tempDir = createTempDirectory(prefix = "kpb-rename-").toFile()
        try {
            val pm = ProjectManager.create("orig")
            pm.setGroup("pt.old")
            pm.addModule("feature", "feature")
            pm.addSourceFile("feature", "src/main/kotlin/pt/old/feature/F.kt", "package pt.old.feature\nclass F")

            pm.saveConfigurationFolder(tempDir.absolutePath)
            val loaded =
                ProjectManager.loadConfigurationFolder(tempDir.absolutePath, newName = "renamed", newGroup = "pt.new")
            assertEquals("renamed", loaded.project.name)
            assertEquals("pt.new", loaded.project.group)
            val mod = loaded.project.modules.first { it.name == "feature" }
            assertEquals("pt.new.feature", mod.group)
            val file = mod.files.firstOrNull { it.path == "src/main/kotlin/pt/old/feature/F.kt" }
            assertNotNull(file, "Paths are preserved even when group overrides")
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `load fails on malformed json`() {
        val tempFile = kotlin.io.path.createTempFile(prefix = "kpb-bad-", suffix = ".json").toFile()
        tempFile.writeText("{ not-json }")
        try {
            assertFailsWith<Exception> { ProjectManager.loadConfiguration(tempFile.absolutePath) }
        } finally {
            tempFile.delete()
        }
    }

    @Test
    fun `load from folder handles missing project file`() {
        val tempDir = createTempDirectory("kpb-missing-").toFile()
        try {
            assertFailsWith<Exception> { ProjectManager.loadConfigurationFolder(tempDir.absolutePath) }
        } finally {
            tempDir.deleteRecursively()
        }
    }

    @Test
    fun `invalid ref path is rejected`() {
        val tempDir = createTempDirectory(prefix = "kpb-badref-").toFile()
        File(tempDir, "project.json").writeText(
            """
            {
              "name": "x",
              "modules": [],
              "versionCatalog": {},
              "gradleFiles": [],
              "kpbFiles": [ { "path": "README.md", "ref": "../escape" } ]
            }
            """.trimIndent()
        )
        assertFailsWith<IllegalArgumentException> {
            ProjectManager.loadConfigurationFolder(tempDir.absolutePath)
        }
        tempDir.deleteRecursively()
    }

    @Test
    fun `checksum mismatch fails load`() {
        val tempDir = createTempDirectory(prefix = "kpb-badchk-").toFile()
        val filesDir = File(tempDir, "files").apply { mkdirs() }
        File(filesDir, "f_0").writeText("ok")
        File(tempDir, "project.json").writeText(
            """
            {
              "name": "x",
              "modules": [],
              "versionCatalog": {},
              "gradleFiles": [],
              "kpbFiles": [ { "path": "README.md", "ref": "f_0", "checksum": "deadbeef" } ]
            }
            """.trimIndent()
        )
        assertFailsWith<IllegalArgumentException> {
            ProjectManager.loadConfigurationFolder(tempDir.absolutePath)
        }
        tempDir.deleteRecursively()
    }

    @Test
    fun `inline content still loads when ref is absent`() {
        val tempFile = kotlin.io.path.createTempFile(prefix = "kpb-inline-only-", suffix = ".json").toFile()
        tempFile.writeText(
            """
            {
              "name": "x",
              "versionCatalog": {},
              "modules": [],
              "gradleFiles": [],
              "kpbFiles": [ { "path": "README.md", "content": "hi" } ]
            }
            """.trimIndent()
        )
        val pm = ProjectManager.loadConfiguration(tempFile.absolutePath)
        val file = pm.project.kpbFiles.firstOrNull { it.path == "README.md" }
        assertNotNull(file)
        assertEquals("hi", file.content)
        tempFile.delete()
    }
}
