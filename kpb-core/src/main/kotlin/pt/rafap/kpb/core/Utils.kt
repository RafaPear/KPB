package pt.rafap.kpb.core

import pt.rafap.kpb.core.gradle.GradleFile
import java.io.File

operator fun List<GradleFile>.plus(other: List<GradleFile>): List<GradleFile> =
    (this + other).runningReduce { a, b -> if (a.name == b.name) a + b else a }