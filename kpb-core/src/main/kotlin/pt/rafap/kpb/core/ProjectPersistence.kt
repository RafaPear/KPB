package pt.rafap.kpb.core

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import pt.rafap.kpb.core.gradle.GradleFile
import pt.rafap.kpb.core.gradle.VersionCatalog
import pt.rafap.kpb.core.gradle.content.*
import pt.rafap.kpb.core.module.Module
import pt.rafap.kpb.core.project.KpbFile
import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.utils.LogManager
import java.io.File

/**
 * Persistence layer for saving and loading project configurations.
 * Saves complete project state including templates, gradle files, and all files needed for rebuild.
 */
object ProjectPersistence {
    private val logger = LogManager.getLogger("ProjectPersistence")

    // DTOs
    data class VersionCatalogDTO(
        val libs: List<LibDTO> = emptyList(),
        val versions: List<VersionDTO> = emptyList(),
        val plugins: List<PluginDTO> = emptyList()
    ) {
        fun toModel(): VersionCatalog = VersionCatalog(
            libs = libs.map { it.toModel() },
            versions = versions.map { it.toModel() },
            plugins = plugins.map { it.toModel() }
        )

        companion object {
            fun from(model: VersionCatalog) = VersionCatalogDTO(
                libs = model.libs.map { LibDTO.from(it) },
                versions = model.versions.map { VersionDTO.from(it) },
                plugins = model.plugins.map { PluginDTO.from(it) }
            )
        }
    }

    data class LibDTO(
        val name: String,
        val id: String,
        val versionRef: String,
        val write: Boolean = true,
        val isTest: Boolean = false
    ) {
        fun toModel() = Lib(name = name, id = id, versionRef = versionRef, write = write, isTest = isTest)

        companion object {
            fun from(model: Lib) = LibDTO(model.name, model.id, model.versionRef, model.write, model.isTest)
        }
    }

    data class VersionDTO(val name: String, val version: String) {
        fun toModel() = Version(name, version)

        companion object {
            fun from(model: Version) = VersionDTO(model.name, model.version)
        }
    }

    data class PluginDTO(val name: String, val id: String, val versionRef: String, val apply: Boolean = true) {
        fun toModel() = Plugin(name, id, versionRef, apply)

        companion object {
            fun from(model: Plugin) = PluginDTO(model.name, model.id, model.versionRef, model.apply)
        }
    }

    data class GradleFileDTO(
        val name: String,
        val imports: List<String> = emptyList(),
        val plugins: List<PluginDTO> = emptyList(),
        val otherPlugins: List<OtherPluginDTO> = emptyList(),
        val dependencies: List<String> = emptyList(),
        val libs: List<LibDTO> = emptyList(),
        val modules: List<String> = emptyList(),
        val others: List<String> = emptyList(),
        val repositories: List<String> = emptyList(),
        val versionCatalog: VersionCatalogDTO = VersionCatalogDTO(),
        val ref: String? = null,
        val checksum: String? = null,
        val content: String? = null
    ) {
        fun toModel(moduleResolver: (String) -> Module): GradleFile {
            return GradleFile(
                name = name,
                imports = imports,
                plugins = plugins.map { it.toModel() },
                otherPlugins = otherPlugins.map { it.toModel() },
                dependencies = dependencies.map { Dependency(it) },
                libs = libs.map { it.toModel() },
                modules = modules.map { moduleResolver(it) },
                others = others.map { Other(it) },
                repositories = repositories,
                versionCatalog = versionCatalog.toModel()
            )
        }

        companion object {
            fun from(model: GradleFile, content: String? = null, ref: String? = null, checksum: String? = null) =
                GradleFileDTO(
                    name = model.name,
                    imports = model.imports,
                    plugins = model.plugins.map { PluginDTO.from(it) },
                    otherPlugins = model.otherPlugins.map { OtherPluginDTO.from(it) },
                    dependencies = model.dependencies.map { it.definition },
                    libs = model.libs.map { LibDTO.from(it) },
                    modules = model.modules.map { it.name },
                    others = model.others.map { it.content },
                    repositories = model.repositories,
                    versionCatalog = VersionCatalogDTO.from(model.versionCatalog),
                    content = content,
                    ref = ref,
                    checksum = checksum
                )
        }
    }

    data class OtherPluginDTO(val definition: String, val apply: Boolean = true) {
        fun toModel() = OtherPlugin(definition, apply)

        companion object {
            fun from(model: OtherPlugin) = OtherPluginDTO(model.definition, model.apply)
        }
    }

    data class KpbFileDTO(
        val path: String,
        val ref: String? = null,
        val checksum: String? = null,
        val content: String? = null
    ) {
        fun toModel(actualContent: String? = content): KpbFile = KpbFile(path = path, content = actualContent)

        companion object {
            fun from(model: KpbFile, content: String? = model.content, ref: String? = null, checksum: String? = null) =
                KpbFileDTO(path = model.path, content = content, ref = ref, checksum = checksum)
        }
    }

    data class ModuleDTO(
        val name: String,
        val simpleName: String,
        val group: String,
        val files: List<KpbFileDTO> = emptyList(),
        val gradleFiles: List<GradleFileDTO> = emptyList(),
        val versionCatalog: VersionCatalogDTO = VersionCatalogDTO()
    ) {
        @Suppress("Unused")
        fun toModel(fileResolver: (KpbFileDTO) -> String?, moduleResolver: (String) -> Module): Module {
            val kpbFiles = files.map { it.toModel(fileResolver(it)) }
            val gfModels = gradleFiles.map { dto -> dto.toModel(moduleResolver) }
            return Module(
                name = name,
                simpleName = simpleName,
                group = group,
                files = kpbFiles,
                gradleFiles = gfModels,
                versionCatalog = versionCatalog.toModel()
            )
        }

        companion object {
            fun from(module: Module, refMapper: (String, String?) -> Pair<String, String>): ModuleDTO {
                val filesDto = module.files.map { kpb ->
                    val (ref, checksum) = refMapper("${module.name}/${kpb.path}", kpb.content)
                    KpbFileDTO.from(kpb, content = null, ref = ref, checksum = checksum)
                }
                val gradleDto = module.gradleFiles.map { gf ->
                    val (ref, checksum) = refMapper("${module.name}/${gf.name}", gf.toKbpFile().content)
                    GradleFileDTO.from(gf, content = null, ref = ref, checksum = checksum)
                }
                return ModuleDTO(
                    name = module.name,
                    simpleName = module.simpleName,
                    group = module.group,
                    files = filesDto,
                    gradleFiles = gradleDto,
                    versionCatalog = VersionCatalogDTO.from(module.versionCatalog)
                )
            }
        }
    }

    data class ProjectDTO(
        val name: String,
        val group: String? = null,
        val versionCatalog: VersionCatalogDTO = VersionCatalogDTO(),
        val modules: List<ModuleDTO> = emptyList(),
        val gradleFiles: List<GradleFileDTO> = emptyList(),
        val kpbFiles: List<KpbFileDTO> = emptyList()
    )

    private val mapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .setSerializationInclusion(JsonInclude.Include.NON_NULL)

    // Public API
    fun save(pm: ProjectManager, filename: String): Result<Unit> = runCatching {
        logger.info("Saving project '${pm.project.name}' to file '$filename'")
        val dto = pm.project.toDtoInline()
        File(filename).writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto))
        logger.fine("Successfully saved to '$filename'")
    }

    fun load(filename: String, newName: String? = null, newGroup: String? = null): Result<ProjectManager> =
        runCatching {
            logger.info("Loading project from file '$filename' (newName=$newName, newGroup=$newGroup)")
            val dto: ProjectDTO = mapper.readValue(File(filename))
            dto.toProjectManager(newName, newGroup)
        }

    fun saveToFolder(pm: ProjectManager, folderName: String): Result<Unit> = runCatching {
        logger.info("Saving project '${pm.project.name}' to folder '$folderName'")
        val baseDir = File(folderName)
        if (!baseDir.exists()) baseDir.mkdirs()
        val filesDir = File(baseDir, "files").apply { mkdirs() }
        val dto = pm.project.toDtoWithRefs(filesDir)
        File(baseDir, "project.json").writeText(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(dto))
        logger.fine("Successfully saved to folder '$folderName'")
    }

    fun loadFromFolder(folderName: String, newName: String? = null, newGroup: String? = null): Result<ProjectManager> =
        runCatching {
            logger.info("Loading project from folder '$folderName' (newName=$newName, newGroup=$newGroup)")
            val baseDir = File(folderName)
            val filesDir = File(baseDir, "files")
            val dto: ProjectDTO = mapper.readValue(File(baseDir, "project.json"))
            dto.toProjectManager(newName, newGroup, filesDir)
        }

    // Builders
    private fun Project.toDtoInline(): ProjectDTO {
        logger.fine("Converting project '$name' to inline DTO")
        return ProjectDTO(
            name = name,
            group = group,
            versionCatalog = VersionCatalogDTO.from(versionCatalog),
            modules = modules.map { module ->
                ModuleDTO(
                    name = module.name,
                    simpleName = module.simpleName,
                    group = module.group,
                    files = module.files.map { KpbFileDTO.from(it, content = it.content) },
                    gradleFiles = module.gradleFiles.map { gf ->
                        GradleFileDTO.from(
                            gf,
                            content = gf.toKbpFile().content
                        )
                    },
                    versionCatalog = VersionCatalogDTO.from(module.versionCatalog)
                )
            },
            gradleFiles = gradleFiles.map { gf -> GradleFileDTO.from(gf, content = gf.toKbpFile().content) },
            kpbFiles = kpbFiles.map { KpbFileDTO.from(it, content = it.content) }
        )
    }

    private fun Project.toDtoWithRefs(filesDir: File): ProjectDTO {
        logger.fine("Converting project '$name' to DTO with refs in '${filesDir.path}'")
        var counter = -1
        fun nextRef(): String {
            counter++
            val ref = "f_$counter"
            return ref
        }
        fun writeRef(content: String?): Pair<String, String> {
            val ref = nextRef()
            val safeContent = content ?: ""
            File(filesDir, ref).writeText(safeContent)
            return ref to checksum(safeContent)
        }

        return ProjectDTO(
            name = name,
            group = group,
            versionCatalog = VersionCatalogDTO.from(versionCatalog),
            modules = modules.map { module ->
                ModuleDTO.from(module) { _, content -> writeRef(content) }
            },
            gradleFiles = gradleFiles.map { gf ->
                val (ref, checksum) = writeRef(gf.toKbpFile().content)
                GradleFileDTO.from(gf, content = null, ref = ref, checksum = checksum)
            },
            kpbFiles = kpbFiles.map { kpb ->
                val (ref, checksum) = writeRef(kpb.content)
                KpbFileDTO.from(kpb, content = null, ref = ref, checksum = checksum)
            }
        )
    }

    // DTO -> Project
    private fun ProjectDTO.toProjectManager(
        newName: String?,
        newGroup: String?,
        filesDir: File? = null
    ): ProjectManager {
        logger.fine("Converting DTO to ProjectManager (name=${newName ?: name})")
        val pm = ProjectManager.create(newName ?: name)
        if (!group.isNullOrBlank() || !newGroup.isNullOrBlank()) {
            pm.setGroup(newGroup ?: group!!)
        }

        // version catalog
        versionCatalog.toModel().also { pm.applyVersionCatalog(it) }

        // root gradle files
        gradleFiles.forEach { dto ->
            val content = resolveContent(dto.ref, dto.content, dto.checksum, filesDir) ?: ""
            pm.project += KpbFile(dto.name, content)
        }

        // root kpb files
        kpbFiles.forEach { dto ->
            val content = resolveContent(dto.ref, dto.content, dto.checksum, filesDir) ?: ""
            pm.project += KpbFile(dto.path, content)
        }

        // first pass: register all modules so cross-module references resolve
        modules.forEach { moduleDto ->
            pm.addModule(moduleDto.name, moduleDto.simpleName)
        }

        // second pass: populate module content and gradle metadata
        modules.forEach { moduleDto ->
            moduleDto.files.forEach { fileDto ->
                val content = resolveContent(fileDto.ref, fileDto.content, fileDto.checksum, filesDir) ?: ""
                val moduleName = moduleDto.name
                if (fileDto.path.contains("/resources/")) {
                    pm.addResourceFile(moduleName, fileDto.path, content)
                } else {
                    pm.addSourceFile(moduleName, fileDto.path, content)
                }
            }

            moduleDto.gradleFiles.forEach { gfDto ->
                val gf = gfDto.toModel { name ->
                    pm.project.modules.find { it.name == name }
                        ?: throw IllegalArgumentException("Module '$name' not found while resolving gradle file")
                }
                val patch = Module(
                    name = moduleDto.name,
                    simpleName = moduleDto.simpleName,
                    group = moduleDto.group,
                    files = emptyList(),
                    gradleFiles = listOf(gf),
                    versionCatalog = gf.versionCatalog
                )
                pm.project += patch
            }

            // module version catalog merge stays scoped to the module
            val moduleCatalog = moduleDto.versionCatalog.toModel()
            if (moduleCatalog != VersionCatalog()) {
                val patch = Module(
                    name = moduleDto.name,
                    simpleName = moduleDto.simpleName,
                    group = moduleDto.group,
                    files = emptyList(),
                    gradleFiles = emptyList(),
                    versionCatalog = moduleCatalog
                )
                pm.project += patch
            }
        }

        return pm
    }

    private fun resolveContent(ref: String?, inline: String?, checksum: String?, filesDir: File?): String? {
        if (inline != null) return inline
        if (ref != null && filesDir != null) {
            require(ref.matches(Regex("^[A-Za-z0-9_.-]+$"))) { "Invalid ref path" }
            val f = File(filesDir, ref)
            if (f.exists()) {
                val content = f.readText()
                if (checksum != null) {
                    val actualChecksum = checksum(content)
                    if (actualChecksum != checksum) {
                        logger.warning("Checksum mismatch for ref '$ref'. Expected: $checksum, Actual: $actualChecksum")
                        throw IllegalArgumentException("Checksum mismatch")
                    }
                }
                return content
            } else {
                logger.warning("Referenced file '$ref' not found in '${filesDir.path}'")
            }
        }
        return null
    }

    private fun checksum(content: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(content.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
