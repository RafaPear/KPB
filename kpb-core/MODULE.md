#Module kpb-core

Core domain for KPB: project composition DSL and types to represent modules, Gradle files, version catalogs, and templates.

#Package pt.rafap.kpb.core.project
Project, KpbFile, materialization helpers.

#Package pt.rafap.kpb.core.module
Module definitions and build scopes.

#Package pt.rafap.kpb.core.gradle
GradleFile and VersionCatalog utilities.

#Package pt.rafap.kpb.core.templates
Template and TemplateBuilderScope.

## Usage
Compose a Project from templates and then materialize it:

```kotlin
val project = Project(
    name = "demo",
    group = "com.example",
    versionCatalog = VersionCatalog(),
    modules = listOf(),
    gradleFiles = listOf(),
    kpbFiles = listOf(),
    templates = listOf()
)
val parsed = project.parseProject()
parsed.createProject(root = File("./demo"))
```
