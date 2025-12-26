package pt.rafap.kpb.core

import kotlin.test.Test
import kotlin.test.assertTrue

class TemplateApplicationTest {

    @Test
    fun `default template applied to project`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("app", "app")
        pm.applyDefaultTemplate("1.0.0")

        val gradleRoot = pm.project.gradleFiles.firstOrNull { it.name.contains("build.gradle") }
        assertTrue(gradleRoot != null, "Root gradle file should be added")
    }

    @Test
    fun `app template applied to modules`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("app", "app")
        pm.applyAppTemplate(listOf("app"))

        val module = pm.project.modules.first()
        assertTrue(module.gradleFiles.isNotEmpty(), "Module gradle file should be enriched")
    }

    @Test
    fun `compose template applied to modules`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.addModule("app", "app")
        pm.applyComposeTemplate(listOf("app"))

        val module = pm.project.modules.first()
        assertTrue(module.gradleFiles.isNotEmpty(), "Module gradle file should be enriched with compose")
    }

    @Test
    fun `dokka template adds gradle configuration`() {
        val pm = ProjectManager.create("p").setGroup("pt.x")
        pm.applyDokkaTemplate()

        assertTrue(pm.project.gradleFiles.isNotEmpty(), "Root gradle files should include dokka")
    }
}

