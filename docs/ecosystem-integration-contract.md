# Earth on Minecraft Ecosystem Integration Contract

## Verified projects

- `earth_on_minecraft`: realistic materials and geology core, reviewed at `0.1.9`.
- `earth_human`: independent human-status mod, reviewed at `0.1.15`.
- `earth_online_xuanhuan`: independently playable Xuanhuan Earth on Minecraft; shares the mana pool when installed together.

Every integration remains optional. Fantasy Earth on Minecraft must retain a complete vanilla fallback when every other ecosystem mod is absent.

## Earth on Minecraft material surface

Arcane facilities and recipes consume these item tags without linking core Java classes:

- `#earth_on_minecraft:spiritual_mineral_substrates`
- `#earth_on_minecraft:arcana_geology_catalysts`
- `#earth_on_minecraft:mana_conductors`
- `#earth_on_minecraft:aether_crystal_substrates`

When a tag is empty, its enhanced facility recipe is omitted from JEI and vanilla fallback routes remain available.

## Shared Arcana player data

Magic and cultivation communicate through `earth_online_arcana.*` persistent keys for current mana, route bonuses, progression and human adaptation. Each independent mod only adds its own contribution and never resets another mod's data.

`earth_human` already reads cultivation, magic research, fasting, breathing, endurance and body-tempering keys through `ArcanaHumanBridge`.

## Current human-recovery boundary

`earth_human` 0.1.15 exposes `EarthHumanApi v1`. Magic calls the snapshot and server-authoritative recovery entry points through an optional reflection adapter; Earth Human owns configuration multipliers, current body limits, migration, persistence and synchronization.

If the API is absent or too old, human integration is disabled safely. Magic must not fall back to reading or writing private `earth_human.*` NBT.

## Compatibility rules

1. Do not compile against another ecosystem mod's source code.
2. Use tags for materials and shared persistent keys for progression.
3. Preserve a vanilla fallback for every enhanced material route.
4. Show detected integrations in the handbook using player-facing language.
5. Update this contract and both language surfaces when shared keys or tags change.
6. A release must pass a standalone world-start and starter-loop test with every optional ecosystem mod absent.
