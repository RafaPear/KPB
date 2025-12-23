# Module kpb-core

Core domain for KPB: project composition DSL and types to represent modules, Gradle files, version catalogs, and templates.

## Packages
- `pt.rafap.kpb.core.project`: Project, KpbFile, materialization helpers.
- `pt.rafap.kpb.core.module`: Module definitions and build scopes.
- `pt.rafap.kpb.core.gradle`: GradleFile and VersionCatalog utilities.
- `pt.rafap.kpb.core.templates`: Template and TemplateBuilderScope.

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
