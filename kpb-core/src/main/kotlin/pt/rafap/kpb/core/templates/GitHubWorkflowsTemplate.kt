package pt.rafap.kpb.core.templates

import pt.rafap.kpb.core.project.Project
import pt.rafap.kpb.core.templates.Template.Companion.buildTemplate

fun Project.createDocsWorkflowTemplate(): Template = this.buildTemplate {
    fileTemplate(".github/workflows/docs.yml") {
        $$"""
name: Deploy Dokka Docs


on:
  push:
    branches:
      - master
      - main
  release:
    types: [ created ]

permissions:
  contents: write

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew

      - name: Build Project
        run: ./gradlew build --no-daemon -x test

      - name: Debug Dokka output
        run: |
          echo "=== Conteúdo de build/dokka/html ==="
          ls -la build/dokka/html || true
          echo "✅ index.html deve estar na raiz agora"

      - name: Upload docs artifact (opcional)
        uses: actions/upload-artifact@v4
        with:
          name: dokka-html
          path: build/dokka/html

      - name: Deploy to GitHub Pages
        uses: peaceiris/actions-gh-pages@v4
        with:
          github_token: ${{ secrets.GITHUB_TOKEN }}
          publish_dir: build/dokka/html
          commit_message: "Deploy Dokka docs"
          keep_files: false
        """.trimIndent()
    }
}

fun Project.createTestsWorkflowTemplate(): Template = this.buildTemplate {
    fileTemplate(".github/workflows/release-tests.yml") {
        """
name: Release Tests

on:
  push:
    branches:
      - master
      - main
  release:
    types: [ created ]

permissions:
  contents: read

jobs:
  test:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup virtual audio device
        run: |
          sudo apt-get update -qq
          sudo apt-get install -y pulseaudio alsa-utils
          # Start PulseAudio in daemon mode
          pulseaudio --start --daemonize=true --exit-idle-time=-1
          # Create a dummy audio sink
          pactl load-module module-null-sink sink_name=dummy
          # Set as default
          pactl set-default-sink dummy
          # Verify audio is available
          pactl info

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Grant execute permission for gradlew
        run: chmod +x ./gradlew

      - name: Run tests
        run: ./gradlew test --no-daemon
        env:
          # Ensure audio server is accessible
          PULSE_SERVER: unix:/run/user/1001/pulse/native

      - name: Upload test reports
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: test-reports
          path: |
            build/reports/tests/test
            build/test-results/test
        """.trimIndent()
    }
}

fun Project.createArtifactsWorkflowTemplate(): Template = this.buildTemplate {
    fileTemplate(".github/workflows/build-artifacts.yml") {
        $$"""
name: Release Workflow

on:
  release:
    types: [ created ]

permissions:
  contents: write  # necessário para upload do artefacto

jobs:
  release:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Setup Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 21

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Make Gradle executable
        run: chmod +x ./gradlew

      - name: Build Project JAR (sem testes)
        run: ./gradlew clean build -x test --no-daemon

      - name: Upload JAR to GitHub Release
        uses: softprops/action-gh-release@v2
        with:
          files: 'build/libs/*.jar'
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
        """.trimIndent()
    }
}

