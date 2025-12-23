# Module kpb-cli

Command-line interface for KPB using KtFlag for argument parsing.

## Packages
- `pt.rafap.kpb.cli`: CLI entry points and commands.

## Usage
Build and run:

```sh
./gradlew :kpb-cli:build
java -jar kpb-cli/build/libs/kpb-cli-1.0.1.jar
```

> Note: The CLI uses `KtFlag` (https://github.com/rafapear/KtFlag) to reduce boilerplate for argument parsing.
