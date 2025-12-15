package pt.rafap.kpb.core

import pt.rafap.kpb.core.project.Project.Companion.buildProject
import pt.rafap.kpb.core.templates.createDefaultTemplate
import pt.rafap.kpb.core.templates.createDokkaTemplate

fun main() {
    val pfs = buildProject("SampleProject") {
        group("com.example")

        module("app") {
            srcMainFile("app/Main.kt") {
                """
            package com.example.app

            fun main() {
                println("Hello, KPB!")
            }
            """.trimIndent()
            }
        }

        file("README.md") {
            """
            # Sample Project

            This is a sample project generated using KPB.
            """.trimIndent()
        }

        template { it.createDokkaTemplate() + it.createDefaultTemplate() }
    }

    pfs.printStructure()

    pfs.createProject()
}