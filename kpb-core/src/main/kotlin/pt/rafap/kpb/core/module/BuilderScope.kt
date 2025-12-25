package pt.rafap.kpb.core.module

import pt.rafap.kpb.core.gradle.GradleFileBuildScope

interface BuilderScope {
    fun fileModule(path: String, content: () -> String? = { null })
    fun resourceFile(path: String, content: () -> String? = { null })
    fun srcMainFile(path: String, content: () -> String? = { null })
    fun srcTestFile(path: String, content: () -> String? = { null })
    fun srcFile(srcName: String, path: String, content: () -> String? = { null })
    fun buildGradleModule(func: GradleFileBuildScope.() -> Unit)
}
