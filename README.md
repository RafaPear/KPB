# Kotlin Project Builder (KPB)

Kotlin Project Builder (KPB) is a multi-module toolkit to scaffold and build Kotlin projects quickly and consistently.
It provides a small DSL and utilities to compose modules, Gradle files, version catalogs, and arbitrary files, then
materialize them on disk. It includes a CLI and an app entry for interactive use.

- Core concepts: Project, Module, GradleFile, VersionCatalog, Templates.
- Modules: `kpb-core`, `kpb-utils`, `kpb-storage`, `kpb-cli`, `kpb-app`.

## Features

- Compose a project from reusable templates and modules.
- Merge Gradle files and version catalogs safely.
- Storage abstractions for saving/loading domain state.
- CLI for quick interactions; app entry for richer UX.

## Requirements

- JDK 21+
- Gradle (the wrapper is included)

## Installation

You can build the project and use the generated JARs under `build/libs` and per-module `build/libs`.

```sh
./gradlew clean build
```

Alternatively, run specific modules:

```sh
./gradlew :kpb-cli:build
./gradlew :kpb-app:build
```

## Quick Start

### Run the CLI

After building, run the CLI JAR:

```sh
java -jar kpb-cli/build/libs/kpb-cli-1.0.1.jar
```

- Uses KtFlag for argument parsing.
- Type `help` inside the CLI to list commands.

### Run the App

After building, run the app JAR:

```sh
java -jar kpb-app/build/libs/kpb-app-1.0.1.jar
```

## Modules Overview

- kpb-core: Core domain (Project, Module, GradleFile, VersionCatalog, Templates) and DSL to compose projects.
- kpb-utils: Utilities (configuration loader, environment helpers, formatting, audio helpers).
- kpb-storage: Sync and async storage abstractions and file-based implementations.
- kpb-cli: Command-line interface entry points and commands.
- kpb-app: App entry point that wires the core.

## Developing

- Use the Gradle wrapper for all tasks.
- Follow KDoc standards (see below) for public APIs.

Common tasks:

```sh
./gradlew build
./gradlew test
```

## Documentation (Dokka)

Generate API docs:

```sh
./gradlew dokkaHtml
```

- Output is under `build/dokka/html/index.html` and per-module `build/dokka-module/html`.
- Module overviews come from each module's `MODULE.md`.

## KDoc Standards

- Provide a concise summary on classes and functions.
- Document non-trivial parameters (`@param`) and return values (`@return`).
- Use `@throws` for expected exceptions and IO semantics.
- Data classes: use `@property` for key fields.
- Include a small usage example when helpful.

## Release & Versioning

- JARs are versioned via Gradle and placed under `build/libs` and per-module `build/libs`.
- Update `gradle.properties` or module `build.gradle.kts` to bump versions.

## Contributing

- Open PRs with tests and KDoc.
- Run Dokka and ensure there are no blocking warnings before merging.

## Links

- Local docs: `build/dokka/html/index.html` after running Dokka.
- Module docs: `kpb-*/build/dokka-module/html/index.html`.

## Roadmap & Current Status

KPB is under active development. Here’s what’s working today and what’s next:

- Current state
    - Core: Functional and tested. You can compose Projects, merge Gradle files and version catalogs, and materialize to
      disk.
    - CLI: Starts, but commands are minimal/stubbed. No user-facing features implemented yet.
    - App: Starts, but no features implemented yet.
    - Storage/Utils: Core interfaces and basic file-backed implementations/utilities are available and tested.

- Near-term milestones
    1. CLI MVP
        - Project scaffold command (generate from template)
        - Config management (read/update settings)
        - Help and command discovery
    2. App MVP
        - Interactive wizard to create a new project
        - Preview virtual structure before writing to disk
    3. Templates & DSL enhancements
        - Built-in templates for common Kotlin/JVM patterns
        - Higher-level helpers for Gradle configuration
    4. Docs & Examples
        - End-to-end examples in docs (core + CLI)
        - Publish API docs (Dokka)

- Priorities
    - Stabilize public API in `kpb-core`
    - Ship CLI scaffold flow
    - Keep KDoc coverage high and CI checks green

If you’re interested in contributing, focus on CLI commands and app flows first—those unlock most user value.
