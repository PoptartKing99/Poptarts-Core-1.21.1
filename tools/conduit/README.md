# Conduit development tools

These tools and the heart-only resource pack are for manual testing and are not
included in the mod JAR.

- `analyze-conduit-heartbeat.py`: reports beat peaks from a mono 16-bit PCM WAV.
- `build-conduit-test-pack.ps1`: rebuilds the transparent test textures and writes
  `build/Conduit Heart Test.zip`. Copy that ZIP into `run/resourcepacks` when needed.
- `resourcepacks/Conduit Heart Test`: source files for the optional heart-only pack.

The production audio generator remains at `scripts/build-conduit-single-heartbeat.ps1`.
It needs an ffmpeg executable and the original vanilla conduit ambient OGG supplied
through its `-Ffmpeg` and `-SourceAudio` parameters. Its output is a 0.8-second clip;
the Java timing definition schedules a one-second pause after it.
