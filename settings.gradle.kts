rootProject.name = "kpb"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

plugins { id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0" }

include("kpb-core", "kpb-cli", "kpb-storage", "kpb-utils", "kpb-app")
