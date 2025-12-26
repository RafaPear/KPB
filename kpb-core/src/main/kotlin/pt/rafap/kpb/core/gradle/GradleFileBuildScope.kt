package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.*
import pt.rafap.kpb.core.module.Module


/**
 * DSL scope for building a [GradleFile].
 *
 * Allows adding libraries, plugins, dependencies, and arbitrary content blocks to a Gradle file.
 */
class GradleFileBuildScope(val name: String) : BuilderScope {
    private val imports = mutableListOf<String>()
    private val plugins = mutableListOf<Plugin>()
    private val otherPlugins = mutableListOf<OtherPlugin>()
    private val libraries = mutableListOf<Lib>()
    private val dependencies = mutableListOf<Dependency>()
    private val modules = mutableListOf<Module>()
    private val versions = mutableListOf<Version>()
    private val others = mutableListOf<Other>()
    private val repositories = mutableListOf<String>()

    /**
     * Adds a library dependency to the Gradle file and registers it in the version catalog.
     */
    override fun library(name: String, id: String, write: Boolean, isTest: Boolean, version: () -> Version) {
        val ver = version()
        versions.add(ver)
        libraries.add(Lib(name, id, ver.name, write, isTest))
    }

    /**
     * Adds a module dependency to the Gradle file.
     */
    override fun moduleGradle(module: () -> Module) {
        modules.add(module())
    }

    override fun moduleGradle(module: Module) {
        modules.add(module)
    }

    /**
     * Adds a plugin to the Gradle file and registers it in the version catalog.
     */
    override fun plugin(name: String, id: String, apply: Boolean, version: () -> Version) {
        val ver = version()
        versions.add(ver)
        plugins.add(Plugin(name, id, ver.name, apply))
    }

    /**
     * Adds a plugin configuration string directly (e.g. for plugins not in the catalog).
     */
    override fun otherPlugin(definition: String, apply: Boolean) {
        otherPlugins.add(OtherPlugin(definition, apply))
    }

    /**
     * Adds a raw dependency string to the dependencies block.
     */
    override fun dependency(dependency: () -> Dependency) {
        dependencies.add(dependency())
    }

    /**
     * Adds arbitrary content to the Gradle file (e.g. task configuration).
     */
    override fun other(content: () -> String) {
        others.add(Other(content()))
    }

    /**
     * Adds an import statement to the Gradle file.
     */
    override fun import(path: String) {
        imports.add(path)
    }

    override fun repository(repository: String) {
        repositories.add(repository)
    }

    override fun buildGradleFile(): GradleFile {
        return GradleFile(
            name,
            imports,
            plugins,
            otherPlugins,
            dependencies,
            libraries,
            modules,
            others,
            repositories,
            buildVersionCatalog()
        )
    }

    private fun buildVersionCatalog(): VersionCatalog {
        return VersionCatalog(
            libs = libraries.distinct(),
            versions = versions.distinct(),
            plugins = plugins.distinct()
        )
    }
}