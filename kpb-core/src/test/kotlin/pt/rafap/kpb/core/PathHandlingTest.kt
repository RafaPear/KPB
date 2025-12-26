package pt.rafap.kpb.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PathHandlingTest {

    @Test
    fun `addSourceFile with fully qualified path stays stable`() {
        val pm = ProjectManager.create("p")
        pm.setGroup("pt.x")
        pm.addModule("lib", "lib")
        val path = "src/main/kotlin/pt/x/lib/A.kt"
        pm.addSourceFile("lib", path, "package pt.x.lib\nclass A")

        val module = pm.project.modules.first()
        val file = module.files.firstOrNull { it.path == path }
        assertNotNull(file, "File should be stored with exact path")
    }

    @Test
    fun `resource files keep path under resources`() {
        val pm = ProjectManager.create("p")
        pm.setGroup("pt.x")
        pm.addModule("lib", "lib")
        val resPath = "config/settings.json"
        pm.addResourceFile("lib", resPath, "{}")

        val file = pm.project.modules.first().files.first()
        assertTrue(file.path.startsWith("src/main/resources"))
        assertTrue(file.path.endsWith(resPath))
    }

    @Test
    fun `path with group relative is expanded when bare`() {
        val pm = ProjectManager.create("p")
        pm.setGroup("pt.x")
        pm.addModule("lib", "lib")
        pm.addSourceFile("lib", "Foo.kt", "class Foo")

        val file = pm.project.modules.first().files.first()
        assertEquals("src/main/kotlin/pt/x/lib/Foo.kt", file.path)
    }

    @Test
    fun `windows style separators are preserved`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("lib", "lib")
        val winPath = "src\\main\\kotlin\\pt\\x\\lib\\Win.kt"
        pm.addSourceFile("lib", winPath, "class Win")

        val file = pm.project.modules.first().files.first()
        assertEquals(winPath, file.path)
    }

    @Test
    fun `absolute looking path is not altered`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("lib", "lib")
        val absolutePath = "/tmp/custom/File.kt"
        pm.addSourceFile("lib", absolutePath, "class Abs")

        val file = pm.project.modules.first().files.first()
        assertEquals(absolutePath, file.path)
    }
}
