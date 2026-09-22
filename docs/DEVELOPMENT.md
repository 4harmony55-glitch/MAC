# Development notes

## Repository handoff

This source tree is designed for manual upload into the empty `4harmony55-glitch/MAC` repository. Upload the contents of this directory at repository root; do not nest the project under another `MAC/` directory.

## Build

The GitHub workflow provisions Java 17, Gradle 8.10.2, and Android SDK 35, then builds `:app:assembleDebug`.

## Verification boundary

The current execution environment has Kotlin/JVM tooling but no installed Android SDK or Gradle distribution. The project therefore receives structural/source checks here; the authoritative Android compilation is the GitHub Actions build defined in `.github/workflows/android.yml`.
