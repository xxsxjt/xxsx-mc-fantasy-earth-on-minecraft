# Fantasy Earth on Minecraft / 魔幻地球 on Minecraft

`Fantasy Earth on Minecraft` is an independently playable fantasy mod. Its stable technical namespace remains `earth_online_magic` to preserve existing test worlds.

It has its own starter materials, world generation, progression, creatures, settlements and vanilla fallback routes. Earth on Minecraft, Earth Human and Xuanhuan Earth on Minecraft only add optional material, body-system and shared-mana integrations; none of them is required to play this mod.

Initial target:

- Minecraft: 26.2
- Loader: NeoForge 26.2.0.7-beta
- Java: 25
- Current mod id: `earth_online_magic`
- Current version: `0.7.0-alpha.1`
- Current status: in-development playable beta

`0.4.3` begins the large-mod phase with a configurable arcane-panel key (`M` by default).
Players can switch researched circuits and perform one attunement anywhere at 72% base
efficiency. The focus mat is now an optional full-efficiency, continuous-focus aid rather than
a progression gate. Position, cooldown, support state and rewards remain server-authoritative.

`0.6.0-alpha.1` adds the first complete creature slice. The Runic Watcher is an original
hostile arcane construct with its own entity type, attributes, combat AI, model, animation,
drops, spawn egg and mountain spawn rules. A reloadable settlement catalog now defines witch
hamlets, goblin exchanges, academy outposts, dwarven delvings and elven groves. These entries
are groundwork for structure pools, residents and trade; physical settlements are not generated
in this alpha.

`0.4.2` adds the shared mana spending contract, closes full-mana cooldown bypasses, accepts
Earth on Minecraft material tags directly in arcane facilities, and reports live optional
integration status in the handbook. Earth Human recovery now uses its public API instead of
writing private NBT or assuming fixed body-part capacities.

Design documents:

- [Initial Plan](docs/initial-plan.md)
- [Shared Mana and Magic](docs/shared-mana-and-magic.md)
- [Arcana Field and Meditation Plan](../earth_online_xuanhuan/docs/arcana-field-and-meditation-plan.md)
- [Ecosystem Integration Contract](docs/ecosystem-integration-contract.md)
- [Large-Scale Development Plan](docs/large-scale-development-plan.md)

## First MVP

Implemented in `neoforge-26.2/`:

- Creative tab: `Fantasy Earth on Minecraft` / `魔幻地球 on Minecraft`
- Starter handbook: `field_arcane_notebook`, craftable from one dirt, any planks, or stone crafting materials
- Arcane initiation notes: `arcane_initiation_notes`, crafted from the handbook plus arcane dust; first use unlocks magic-route contribution to the shared mana value
- Basic materials: arcane dust, rune ink, ritual chalk, crystallized mana salt, aether glass, rune copper plate, aether crystal, dormant ritual core
- Basic blocks: alchemy table, rune carving table, ritual pedestal, aether crystal cluster
- Bilingual language files: `zh_cn` and `en_us`
- Vanilla fallback recipes; Earth Online core is optional, not required
- First image-generated texture pass:
  - 4 block textures at 64x64
  - 9 item icons at 32x32 with transparent backgrounds
  - Source generation prompts and raw previews are kept under ignored `tmp/imagegen/`
- First aether-field pass:
  - chunk-level `AetherChunkField`
  - aether crystal / ritual / rune / alchemy source terms
  - field disturbance and focus cooldown
  - notebook, initiation notes, adaptation notes and magic blocks show local field feedback
- Portable attunement:
  - configurable arcane-panel key (`M` by default)
  - free one-cycle attunement anywhere at reduced efficiency
  - a real seated pose with occupancy and cleanup safety
  - a server-synced custom screen plus compact four-stage HUD
  - selectable aether, body-ward and breath-ward focuses gated by learned notes
  - distinct mana, fatigue, body-part, air and temporary ward outcomes

Texture note: facilities use per-face exterior textures, distinct active states and separate visual identities for alchemy, rune carving and rituals. This remains an in-development test build.

Mana note: this mod shares the versioned `earth_online_arcana.*` player-data contract with `earth_online_xuanhuan`. Magic-route and qi-route bonuses add together when both mods are present, but each mod writes only its own contribution and remains fully playable alone. Aether-field recovery is intentionally separate from xuanhuan qi: magic reads crystal, ritual, rune and alchemy structures, while xuanhuan reads veins, springs, spirit soil and arrays.

Build artifact: `fantasy-earth-on-minecraft-neoforge-26.2-0.7.0-alpha.1.jar`.

Build:

```powershell
cd neoforge-26.2
.\gradlew.bat build --no-daemon --offline
```
