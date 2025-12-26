package pt.rafap.kpb.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class ProjectManagerOperationsTest {

    @Test
    fun `cannot add module without group`() {
        val pm = ProjectManager.create("p")
        assertFailsWith<IllegalStateException> { pm.addModule("m", "m") }
    }

    @Test
    fun `add and remove module updates project`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("m", "m")
        assertEquals(1, pm.project.modules.size)
        pm.removeModule("m")
        assertEquals(0, pm.project.modules.size)
    }

    @Test
    fun `add plugins libs and deps accumulate`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("m", "m")
        pm.addPlugin("m", "kotlin", "org.jetbrains.kotlin.jvm", "kotlin", "1.9.0")
        pm.addLibrary("m", "kotlin", "org.jetbrains.kotlin:kotlin-stdlib", "kotlin", "1.9.0")
        pm.addDependency("m", "implementation(\"org.jetbrains.kotlin:kotlin-stdlib\")")

        val module = pm.project.modules.first()
        val gf = module.gradleFiles.firstOrNull()
        assertNotNull(gf, "Gradle file should be present")
        assertEquals(1, gf.plugins.size)
        assertEquals(1, gf.libs.size)
        assertEquals(1, gf.dependencies.size)
    }

    @Test
    fun `module dependency is recorded`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("a", "a")
        pm.addModule("b", "b")
        pm.addModuleDependency("b", "a")

        val modB = pm.project.modules.first { it.name == "b" }
        val gf = modB.gradleFiles.first()
        assertEquals(1, gf.modules.size)
        assertEquals("a", gf.modules.first().name)
    }

    @Test
    fun `addReadme creates root file`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addReadme("hello")
        assertNotNull(pm.project.kpbFiles.firstOrNull { it.path == "README.md" })
    }

    @Test
    fun `changing project group updates module groups`() {
        val pm = ProjectManager.create("p").setGroup("old.group")
        pm.addModule("mod", "mod")

        val moduleBefore = pm.project.modules.first()
        assertEquals("old.group.mod", moduleBefore.group)

        pm.setGroup("new.group")

        val moduleAfter = pm.project.modules.first()
        assertEquals("new.group.mod", moduleAfter.group)
        assertEquals("new.group", pm.project.group)
    }
}
