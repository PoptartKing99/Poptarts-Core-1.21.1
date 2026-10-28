# Custom equipment slots

The player inventory has two ring slots, plus cape, amulet, belt, utility, and back slots. Beside the player preview, the left column is back/cape/belt/vanilla offhand; the right column is amulet/ring/ring/utility. Back is separate from cape and accepts only designated back equipment. The 2x2 crafting grid and its result sit farther right. Inventory, hotbar, armor, crafting, and offhand indices are unchanged. Each extra slot holds one item. No equipment items, effects, or cape renderer are included yet.

Items become eligible through these item tags:

- `poptartcore:equipment/rings`
- `poptartcore:equipment/capes`
- `poptartcore:equipment/amulets`
- `poptartcore:equipment/belts`
- `poptartcore:equipment/utility`
- `poptartcore:equipment/back`

Utility accepts the vanilla spyglass and includes the previous `poptartcore:equipment/accessories` tag so existing assignments continue working. The utility slot does not accept arbitrary inventory items. Both ring slots accept the rings tag; duplicate ring types are allowed for now because there are no effects to balance. Back is appended at storage index 6, preserving existing equipment indices and saved items.

Hold **Z** to use a spyglass equipped in Utility. Rebind **Use Utility Item** under Controls > Key Binds > Gameplay. Releasing the key, opening a screen, losing window focus, removing the spyglass, dying, or using a held item stops utility zoom. First-person zoom, the vanilla scope overlay, mouse sensitivity, and start/stop sounds are client-side; neither hand nor the stored item is changed. This does not synchronize a spyglass-use animation to other players.

`AccessoryInventory` owns serialized storage. The inventory menu resolves it lazily and uses vanilla menu synchronization and click packets. The creative inventory tab exposes the same slots in a right-hand column so they do not overlap creative tabs. Its normal creative slot packets are extended to accept only actual accessory slots, with creative permission, item category, enabled-feature, and single-item limits checked on the server.

`AccessoryCorpseProvider` lets Ragdoll capture and restore these slots. Ragdoll's Take All button restores their original positions. The corpse screen exposes them individually too. With keepInventory enabled, equipment stays on the player and is excluded from the corpse provider. Ordinary death drops handle any equipment not captured by a corpse. Player cloning copies the remaining storage, including return from the End.

Run `scripts/verification/equipment/verify-equipment.ps1` to build and compile the isolated equipment GameTests. Add `-RunServer` to copy the required runtime mods from `run/mods` into the disposable `build/game-test-run/mods` instance and execute the tests. The normal run instance is not changed. The init script selects the equipment smoke suite. Use `scripts/verify-mod.ps1` for the complete automated check.

The eleven server tests cover slot count and restrictions, serialization and player save/load, migration from five slots, cloning storage, shift-click equip/unequip, relocated crafting, corpse capture and restoration, keepInventory, ground-drop fallback, creative packet permissions/category/count checks, spyglass eligibility, and utility zoom activation rules. They do not verify the rendered client screen, client mixin application, keyboard input, sounds, or a real multiplayer connection. The new belt is appended to saved storage, preserving the previous five indices; the renamed necklace keeps the old pendant index.

Client verification still requires opening survival inventory with the recipe book open and closed, using the creative inventory tab, checking empty-slot tooltips, and testing click/drag/shift-click behavior with tagged equipment. Test reconnect, keepInventory on/off, corpse Take All and individual recovery in multiplayer before shipping.
