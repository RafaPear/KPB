package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.project.KpbFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog

class ModuleBuildScope(val name: String, val groupPath: String?): BuilderScope {
    private val files = mutableListOf<KpbFile>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()

    override fun file(path: String, content: () -> String?) {
        files.add(KpbFile(path, content()))
    }

    private fun buildSrcPath(srcName: String, path: String): String {
        val str = if (groupPath == null) "src/$srcName/$path"
        else "src/$srcName/kotlin/${groupPath}/$path"
        return str
    }

    override fun srcMainFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("main", path)
        file(fullPath, content)
    }

    override fun srcTestFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("test", path)
        file(fullPath, content)
    }

    override fun srcFile(srcName: String, path: String, content: () -> String?) {
        val fullPath = buildSrcPath(srcName, path)
        file(fullPath, content)
    }

    override fun resourceFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("main/resources", path)
        file(fullPath, content)
    }

    fun buildGradle(func: GradleFileBuildScope.() -> Unit) {
        val scope = GradleFileBuildScope("build.gradle.kts")
        scope.func()
        val buildFileContent = scope.build()
        versionCatalog += buildFileContent.versionCatalog
        gradleFiles.add(buildFileContent)
    }

    fun build(): Module {
        buildGradle {}
        return Module(name, files, gradleFiles, versionCatalog)
    }
}