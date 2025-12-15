package pt.rafap.kpb.core.project

import kotlin.collections.forEach

class ProjectFileSystem(
    private val kbpFiles: List<KbpFile>
) {
    fun createFiles() = kbpFiles.forEach { it.create() }

    override fun toString(): String {
        val builder = StringBuilder()
        kbpFiles.forEach { file ->
            builder.append("File Path: ${file.path}, Content: ${file.content ?: "N/A"}\n")
        }
        return builder.toString()
    }

    companion object {
//        fun buildProjectFileSystem(func: ProjectBuilderScope.() -> Unit): ProjectFileSystem {
//            val kbpFiles = mutableListOf<KbpFile>()
//            val scope = ProjectBuilderScope()
//            scope.func()
//            return ProjectFileSystem(kbpFiles)
//        }

        private fun normalize(path: String): String =
            path.replace("\\", "/").removePrefix("/")
    }
}
