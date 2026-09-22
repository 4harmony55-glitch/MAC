# Reference capability inventory

Static inspection of the supplied APK found three machine-readable automation catalogs under its AI assets. MAC converts only the functional identifiers into its own inventory manifest; it does not bundle the reference YAML files or their prose.

| Type | Observed count | MAC status |
|---|---:|---|
| Actions | 207 | Cataloged; adapters added incrementally |
| Triggers | 122 | Cataloged; adapters added incrementally |
| Constraints | 87 | Cataloged; typed evaluator being implemented incrementally |
| **Total** | **416** | |

The inventory is an implementation roadmap, not a claim that all 416 capabilities are already executable. MAC reports unsupported adapters explicitly instead of silently treating them as successful.
