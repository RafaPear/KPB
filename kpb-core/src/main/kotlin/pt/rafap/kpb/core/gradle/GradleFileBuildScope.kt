package pt.rafap.kpb.core.gradle

import pt.rafap.kpb.core.gradle.content.*
import pt.rafap.kpb.core.module.Module


class GradleFileBuildScope(val name: String): BuilderScope {
    private val imports = mutableListOf<String>()
    private val plugins = mutableListOf<Plugin>()
    private val otherPlugins = mutableListOf<OtherPlugin>()
    private val libraries = mutableListOf<Lib>()
    private val dependencies = mutableListOf<Dependency>()
    private val modules = mutableListOf<Module>()
    private val versions = mutableListOf<Version>()
    private val others = mutableListOf<Other>()

    override fun library(name: String, id: String, version: () -> Version) {
        val ver = version()
        versions.add(ver)
        libraries.add(Lib(name, ver.name, id))
    }

    override fun module(module: () -> Module) {
        modules.add(module())
    }

    override fun plugin(name: String, id: String, apply: Boolean, version: () -> Version) {
        val ver = version()
        versions.add(ver)
        plugins.add(Plugin(name, id, ver.name))
    }

    override fun otherPlugin(definition: String, apply: Boolean) {
        otherPlugins.add(OtherPlugin(definition, apply))
    }

    override fun dependency(dependency: () -> Dependency) {
        dependencies.add(dependency())
    }

    override fun other(content: () -> String) {
        others.add(Other(content()))
    }

    override fun import(path: String) {
        imports.add(path)
    }

    override fun build(): GradleFile {
        return GradleFile(
            name = name,
            imports = imports,
            plugins = plugins,
            libs = libraries,
            others = others,
            dependencies = dependencies,
            modules = modules,
            otherPlugins = otherPlugins,
            versionCatalog = buildVersionCatalog()
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