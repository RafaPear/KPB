package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.project.KpbFile
import pt.rafap.kpb.core.gradle.GradleFileBuildScope
import pt.rafap.kpb.core.gradle.VersionCatalog

class ModuleBuildScope(val name: String, val simpleName: String, val group: String?) : BuilderScope {
    private val files = mutableListOf<KpbFile>()
    private var versionCatalog: VersionCatalog = VersionCatalog()
    private var gradleFiles = mutableListOf<GradleFile>()
    val groupPath = group?.replace('.', '/')

    override fun fileModule(path: String, content: () -> String?) {
        files.add(KpbFile(path, content()))
    }

    private fun buildSrcPath(srcName: String, path: String): String {
        val str = if (groupPath == null) "src/$srcName/$path"
        else "src/$srcName/kotlin/${groupPath}/$path"
        return str
    }

    override fun srcMainFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("main", path)
        fileModule(fullPath, content)
    }

    override fun srcTestFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("test", path)
        fileModule(fullPath, content)
    }

    override fun srcFile(srcName: String, path: String, content: () -> String?) {
        val fullPath = buildSrcPath(srcName, path)
        fileModule(fullPath, content)
    }

    override fun resourceFile(path: String, content: () -> String?) {
        val fullPath = buildSrcPath("main/resources", path)
        fileModule(fullPath, content)
    }

    override fun buildGradleModule(func: GradleFileBuildScope.() -> Unit) {
        val scope = GradleFileBuildScope("build.gradle.kts")
        scope.func()
        val buildFileContent = scope.buildGradleFile()
        versionCatalog += buildFileContent.versionCatalog
        gradleFiles.add(buildFileContent)
    }

    /**
     * Builds and returns the configured module.
     *
     * @return The built Module instance.
     */
    fun build(): Module {
        return Module(
            name,
            simpleName,
            group ?: "",
            files,
            gradleFiles,
            versionCatalog
        )
    }
}