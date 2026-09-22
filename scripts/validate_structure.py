import json
from pathlib import Path
root=Path(__file__).resolve().parents[1]
required=[
    'settings.gradle.kts','build.gradle.kts','gradle.properties','app/build.gradle.kts',
    'app/src/main/AndroidManifest.xml','app/src/main/assets/capability_catalog.json',
    '.github/workflows/android.yml','README.md'
]
missing=[p for p in required if not (root/p).exists()]
if missing: raise SystemExit(f'Missing required files: {missing}')
data=json.loads((root/'app/src/main/assets/capability_catalog.json').read_text())
assert data['inventory']=={'actions':207,'triggers':122,'constraints':87},data['inventory']
assert sum(len(data[k]) for k in ('actions','triggers','constraints'))==416
for p in root.rglob('*.kt'):
    txt=p.read_text()
    if '\x00' in txt: raise SystemExit(f'NUL byte in {p}')
print('MAC structure: OK')
print('Capability inventory: 416 entries (207 actions / 122 triggers / 87 constraints)')
