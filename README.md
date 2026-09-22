# MAC

MAC is an independent Android automation platform built from a clean-room functional study of an automation reference application.

## Current milestone: Foundation + runtime baseline

- Kotlin + Jetpack Compose
- Trigger → constraint → action macro model
- Persistent local macro storage
- Searchable trigger/constraint/action picker
- Parameter editing for nodes
- Execution trace UI
- Initial concrete actions: notification, wait, variable, clipboard, vibration, speech, media volume, app launch, HTTPS request
- Initial background bridge: boot broadcast (battery monitoring remains a dedicated runtime-adapter milestone)
- Inventory asset containing the reference application's observed automation-type identifiers
- GitHub Actions APK build

## Architecture

```text
UI
 ↓
MacroRepository
 ↓
Domain model
 ↓
ExecutionEngine
 ↓
Android capability adapters
```

The commercial licensing, advertising, branding, and purchase-gating infrastructure of the reference application are not included in MAC.

## Build

GitHub Actions compiles the debug APK on pushes, pull requests, and manual workflow dispatch. The APK is published as `MAC-debug-apk`.

## Reference inventory

The supplied APK contains 207 action definitions, 122 trigger definitions, and 87 constraint definitions. MAC records those functional identifiers in `capability_catalog.json` as an implementation roadmap. MAC does not bundle the reference application's source code or proprietary implementation.
