# Compatibility fixtures

These files protect the on-disk formats used by existing jWorship installations.

- `songs/sk-asc-legacy-song.sng` was generated from the original `sk.asc.worship.Song` serialized shape at commit `b3acdab` (`serialVersionUID` `107484166988955595L`). It verifies the historical `sk.asc.*` to `sk.calvary.*` class-name bridge.
- `settings/generalSettings.ser` and `settings/picturebookmarks.ser` were generated with the pre-FlatLaf Java 21 baseline at commit `d093ba2`.
- `songs/legacy-song.txt` is UTF-8; `songs/legacy-song-cp1250.txt` is a byte-preserved Windows-1250 fixture for historical Slovak/Czech installations.
- `lang/custom-lang.lng` is a deliberately incomplete custom translation file. It verifies that historical keys remain readable while newer UI labels can still use application fallbacks.

The binary fixtures are test inputs, not files generated during the build. Do not rewrite them as part of normal refactoring. If an intentional migration changes a format, add a new fixture and keep the old one until its reader is deliberately retired.
