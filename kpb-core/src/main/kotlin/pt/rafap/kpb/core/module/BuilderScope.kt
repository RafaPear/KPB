package pt.rafap.kpb.core.module

interface BuilderScope {
    fun file(path: String, content: () -> String? = { null })
    fun resourceFile(path: String, content: () -> String? = { null })
    fun srcMainFile(path: String, content: () -> String? = { null })
    fun srcTestFile(path: String, content: () -> String? = { null })
    fun srcFile(srcName: String, path: String, content: () -> String? = { null })
}
