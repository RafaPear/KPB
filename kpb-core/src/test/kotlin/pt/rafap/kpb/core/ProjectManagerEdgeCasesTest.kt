package pt.rafap.kpb.core

import kotlin.io.path.createTempDirectory
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ProjectManagerEdgeCasesTest {

    @Test
    fun `cannot add duplicate module names`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("m", "m")
        assertFailsWith<IllegalArgumentException> { pm.addModule("m", "m") }
    }

    @Test
    fun `cannot add duplicate module simpleName`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("feature-api", "feature")
        assertFailsWith<IllegalArgumentException> { pm.addModule("feature-impl", "feature") }
    }

    @Test
    fun `remove missing module throws`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        assertFailsWith<IllegalArgumentException> { pm.removeModule("missing") }
    }

    @Test
    fun `apply templates with mismatched groups fails`() {
        val pm = ProjectManager.create("p").setGroup("pt.a")
        pm.addModule("a", "a")
        pm.addModule("b", "b")
        // alter one module group manually to create mismatch
        val altered = pm.project.modules.map {
            if (it.name == "b") it.copy(group = "other.b") else it
        }
        pm.project = pm.project.copy(modules = altered)
        assertFailsWith<IllegalArgumentException> { pm.applyAppTemplate(listOf("a", "b")) }
    }

    @Test
    fun `generate is safe when project already materialized`() {
        val tempDir = createTempDirectory(prefix = "kpb-generate-").toFile()
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("m", "m")
        pm.addSourceFile("m", "src/main/kotlin/pt/x/m/A.kt", "class A")
        pm.generate(tempDir.absolutePath)
        // calling generate again should not throw and should overwrite files without errors
        pm.generate(tempDir.absolutePath)

        tempDir.deleteRecursively()
        // we just assert true to mark the test, absence of exception is success
        assertTrue(true)
    }
}
