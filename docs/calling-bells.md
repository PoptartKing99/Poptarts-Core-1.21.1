# Calling bells

Both bells are non-stackable tools in the existing Poptart's Core creative tab. Their models and original textures match the supplied Wayfarer JAR. That JAR supplies no crafting recipes for them, so this implementation does not invent recipes.

Right-click the Allay-Calling Bell to summon three allays. They gather nearby dropped items, prioritizing where you aimed, and deliver them toward you. Each carries up to sixteen matching items while respecting the item's actual stack limit. Using the bell on an animal instead makes the allays borrow one matching food item each from your inventory and lure that animal type. Borrowed food and collected items are returned when helpers are recalled or removed.

Right-click the Vex-Calling Bell to summon five sword-carrying vexes. Summoning applies three points of magic damage to the user. The vexes attack nearby hostiles and prioritize the owner's attacked target or attackers. They exclude their owner and helpers belonging to that owner.

Ringing a bell again recalls its active group. Both bells have no cooldown, so the next use can summon a new group immediately after recall or after the last helper disappears. Losing the bell from your inventory also dismisses its helpers. Idle allays vanish after sixty ticks; idle vexes vanish after two hundred ticks.

## Ownership and integration

The `summoning` package owns helper creation, AI, recall and cleanup. Three mixins apply the relevant AI and phasing behavior only to entities marked as bell summons. Ordinary allays, evokers and their vexes retain their existing behavior. Vanilla entities and their renderers are reused; no additional entity types are registered.

Serializable attachments on the owner store summon groups and member UUIDs. Each helper stores its owner and group IDs. Those records are authoritative even when a helper's chunk unloads. Recall removes the group first, then loaded members across dimensions; an unloaded member checks the group when it returns and dismisses itself if already recalled. Entity death and permanent removal update membership. Temporary unloads do not erase membership or permit duplicate groups.

An alternative based only on scanning loaded entities was rejected because absent entities may simply be in unloaded chunks. Keeping a second world-wide ownership database would duplicate the existing attachment state. Saved attachments follow the mod's existing lifecycle and keep one owner for each record.

Phasing uses the reference particles, sounds, delay and wall traversal. Its saved state also retains the helper's previous visibility, invulnerability and gravity flags, allowing a save made during phasing to finish safely after loading.

## Verification

Run `scripts/verify-mod.ps1` for the full build, server GameTests and packaged-resource checks. `CallingBellGameTests` covers both bells, cooldowns, health cost, owner/type isolation, item collection and delivery, stack limits, animal lure food, recall and death drops, unload/reload cleanup, idle expiry, hostile targeting, vanilla-mob isolation, saved attachments and saved phasing.

Run `scripts/verification/catalog/verify-client.ps1 -RunClient` for the isolated client checks. It loads both actual bell models, verifies their translated names and resolves all five sound events. Automated checks do not confirm perceived sound/visual quality or a real multiplayer connection. In game, check animal following, vex combat, both hand poses, recall sounds, and two-player ownership across reconnects and dimension changes.
