# Module kpb-utils

Utilities for configuration, environment, formatting, and optional audio helpers used across KPB.

## Packages
- `pt.rafap.kpb.utils`: Config, ConfigLoader, Environment, formatters.

## Usage
Load configuration with defaults:

```kotlin
val cfg = ConfigLoader("app.properties") { AppConfig(it) }.loadConfig()
println(cfg.DB_URL)
```
