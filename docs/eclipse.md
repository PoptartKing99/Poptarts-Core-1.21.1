# Eclipses

Eclipses affect the Overworld's visuals and gameplay daylight. Natural events are separated by 20–30 clear Minecraft days. Coverage builds during the morning, reaches totality around noon, and clears at sunset. Commands also provide held previews and short timed cycles for testing. The rendering uses Wayfarer's eight-frame sun atlas, phase brightness levels, cold daylight tint, daytime stars, cloud dimming, and totality fog colors. Clear restores the normal sun and lighting.

The warm totality fog tint is limited to 15% of Wayfarer's original blend strength, keeping distant scenery dark with a subtle warmth. Fog distance is unchanged.

No Man's Land's sun dog halo and rays are hidden whenever any part of the sun is covered, including totality. The effect uses No Man's Land's normal rendering again when the sun is clear. Only rendering is suppressed; the sun dog event itself continues normally and is not restarted when the eclipse ends.

The normal moon is hidden only while the current phase has coverage. Natural events clear at sunset, so the moon rises normally every night. Held covered previews can still hide it at night until cleared. The calendar and underlying lunar phases continue normally; this only skips drawing the moon. The uncovered `new` preview keeps the moon visible.

With cheats or operator permission level 4:

| Command | Result |
| --- | --- |
| `/poptartcore eclipse natural on` | Enable one-day natural eclipses with a randomly chosen 20-30-day clear period. |
| `/poptartcore eclipse natural off` | Disable recurring eclipses and clear the current effect. |
| `/poptartcore eclipse start` | Run a three-minute cycle. |
| `/poptartcore eclipse start 60` | Run a cycle lasting 60 seconds (allowed: 1–3,600). |
| `/poptartcore eclipse phase total` | Hold totality until changed or stopped. |
| `/poptartcore eclipse stop` | Restore normal visuals immediately and leave natural cycling off. |
| `/poptartcore eclipse` | Show the phase, mode, and remaining clear days or the active daylight event. |

Phase names: `clear`, `new`, `waning_gibbous`, `waning_half`, `waning_crescent`, `total`, `waxing_crescent`, `waxing_half`, `waxing_gibbous`. Tab completion lists them. Set the time to noon to see the eclipse clearly; the commands do not move the sun. The effect fades with the sun at dusk and does not darken the night further.

The state is saved with the world. Manual timed cycles use elapsed game ticks, so changing the day time does not skip their phases. Natural waiting periods advance at midnight, including nights passed by sleeping. Natural coverage is calculated from the world's time of day. Reloading preserves the event date and remaining clear days; the visible phase resumes at the saved sun position. Rewinding `/time` rebases the event date or waiting checkpoint while coverage follows the new sun position. Large forward jumps catch up in bounded batches to avoid blocking a server tick. Time spent with the server stopped does not advance either mode. Players joining, respawning, or changing dimensions receive the current phase. Disconnecting clears the client cache.

## Natural eclipse schedule

Each event follows **25% → 50% → 75% → totality → 75% → 50% → 25% → clear** on the same day. The world clock controls the whole schedule.

| World time | Approximate Minecraft clock | Coverage |
| --- | --- | --- |
| Before 1,000 | Before 7 a.m. | Clear |
| 1,000–2,599 | 7:00–8:36 a.m. | 25% |
| 2,600–4,199 | 8:36–10:12 a.m. | 50% |
| 4,200–5,799 | 10:12–11:48 a.m. | 75% |
| 5,800–6,199 | 11:48 a.m.–12:12 p.m. | Total |
| 6,200–8,099 | 12:12–2:06 p.m. | 75% |
| 8,100–9,999 | 2:06–4:00 p.m. | 50% |
| 10,000–11,999 | 4:00–6:00 p.m. | 25% |
| From 12,000 onward | 6:00 p.m. through the night | Clear |

Slowing the world's day clock stretches every stage, and freezing daylight freezes the visible stage. The controller does not advance through natural phases using real-time timers. Better Days 3.3.6.3 for NeoForge 1.21.1 has been checked in the client with the local 90-minute day / 30-minute night profile and normal vanilla sleeping. Better Days remains optional and is not a PoptartCore dependency.

At midnight after an event, the game chooses a new clear period uniformly from 20 through 30 days, inclusive. The same range is chosen when natural mode is first enabled. Waiting days use normal Minecraft sunlight, and the countdown is saved so it is not chosen again on reload or repeated enable commands. After the last clear midnight, the next event remains unobscured until the following morning.

Totality lasts three real minutes with 90-minute days. `/poptartcore eclipse phase total` holds a preview outside the natural schedule. The `new` phase is available for manual preview.

Recurring mode is enabled for new worlds and for the original default off state when upgrading. Old unobscured natural states receive a fresh 20-30-day wait. Existing five-day events retain their event date and adopt the new sun-position schedule. Existing waiting periods keep their remaining days. The new save format stores an event date rather than a daily phase. Existing held previews and timed tests are preserved. An explicit off state stays off across reloads. Using `start` or `phase` pauses the recurring controller; `natural on` re-enables it after testing. Repeating `natural on` while it is already enabled preserves the current cycle and countdown.

For testing, enable natural mode, set the clock to morning with `/time set 1000`, and check `/poptartcore eclipse` for the remaining wait. Skip that many days with `/time add <days>d`. Use `/time add 24d` when 24 clear days remain. Once the event is active, set the clock to a time in the table to view that phase. For example, `/time set noon` shows totality. Adding whole days keeps the sun at the same time of day. A time jump shows the phase at the resulting time and can skip intervening phases or entire events.

## Boundaries

`eclipse` owns the timeline, saved state, commands, synchronization, and shared daylight calculations. `eclipse/client` and the four client mixins own rendering. A common level mixin applies eclipse darkening after the world's normal sky-brightness update. A phase packet is sent when the phase changes, rather than every tick. The lightmap is remapped before its existing upload, with reusable scratch storage.

Effective daylight follows the eclipse phase on the server and client. At clear noon, totality raises sky darkening to 13, leaving exposed sky light at an effective level of 2. Minecraft's normal mob sunlight and spawning checks use that daylight. The stored sky-light values and block lighting are preserved, so torches continue to prevent light-sensitive spawns. Spawn rates, mob caps, and other spawn requirements remain Minecraft's own rules.

Overworld rain and thunder stop during every covered phase. The weather cycle resumes when the sun clears.

Beds retain their normal sleeping hours. The game checks daylight before applying eclipse darkening, so daytime totality keeps the clear-weather bedtime rule. Players already sleeping wake at normal daylight.

This pass does not add Old Sun, madness, or a blanket ban on sleeping during totality. Custom sky renderers and shader packs that replace vanilla's sky or lightmap may need their own integration.

The texture and visual formulas were taken from the supplied `wayfarer_core-0.1.0.jar` (SHA-256 `11eac76b30e17569019beeb1fe33129dd716421a2f9a974bb994674f1dd52bc4`), exported under `mod-comparison-2026-11-05`. No Wayfarer developer-name permission gate is included.

## Verification

Server tests cover the manual timeline, complete daylight events, both wait-range endpoints, slow and frozen clocks, saved countdowns, midnight boundaries, clock jumps, upgrade behavior, commands, gameplay daylight, real mob sunlight/spawn checks, block lighting, normal sleeping hours, and an unchanged world clock. Run them with `./gradlew.bat -PgameTests build`.

Run `./scripts/verification/eclipse/verify-client.ps1 -RunClient` for an isolated client scene. Add `-WithClientRenderMods` to include the local Sodium and Polytone versions from `run/mods`. It sends actual server commands, checks received phases and cloud colors, captures all sun frames and landscape lighting, and checks night and dimension behavior. A forced sun dog in a cold biome verifies the real renderer creates its mesh for unobscured sun, skips rendering during every covered phase, and resumes afterward while its event stays active. Nighttime framebuffer checks verify moon suppression during partial eclipses and totality, and restoration afterward. The runner restores normal client launch arguments afterward. Screenshots and the runtime log are under `build/eclipse-client-run`.

Sky and cloud colors wrap the completed color method, including other mods' return hooks. This preserves No Man's Land's Friend Moon cloud darkening while applying the eclipse afterward; a return-value injection alone can be bypassed by its cancellable hook.

Add `-WithBetterDays` to the client verification command to stage `run/mods/betterdays-1.21.1-3.3.6.3-NEOFORGE.jar` and copy `run/config/betterdays-common.toml` into the isolated instance. Alongside the eclipse checks, this profile measures the real server clock rates, verifies client synchronization, and uses a bed to check respawn setting and the normal skip to morning. It expects the development settings documented in [development.md](development.md).
