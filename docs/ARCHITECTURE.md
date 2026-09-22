# MAC architecture

## Runtime contract

A macro is a declarative workflow consisting of triggers, optional constraints, and ordered actions.

The runtime is intentionally UI-independent. Android-specific functionality will be implemented behind capability adapters as individual modules mature.

## Capability tiers

1. Local/no-permission actions — variables, clipboard, wait, vibration.
2. Runtime-permission actions — notifications, microphone, camera, location, SMS.
3. Special-access actions — accessibility, notification listener, write settings.
4. Privileged adapters — Shizuku/ADB and other device-dependent capabilities.

The engine must fail explicitly when an adapter or capability is unavailable. It must never silently report success.

## Next engineering milestones

- Replace placeholder constraint pass-through with typed constraint evaluation.
- Add a durable execution journal.
- Add scheduler/alarm orchestration for time/interval triggers.
- Add notification-listener trigger adapter.
- Add AccessibilityService adapter and UI-query primitives.
- Add variable resolver with typed values and expression support.
- Add action blocks and nested workflows.
- Add simulator/dry-run and step-through debugging.
