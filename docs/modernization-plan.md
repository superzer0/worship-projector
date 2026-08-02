# Modernization plan

## Goal

Create a maintainable, dependable lyrics-only worship presentation application while protecting existing songs and the operator's live workflow.

The target product slice is deliberately small:

1. manage a local song library;
2. find a song quickly;
3. select the currently sung verse or chorus;
4. preview it;
5. send it to a projector/second display;
6. blank the lyrics immediately when needed.

Legacy multimedia, capture, video effects, and network song synchronization are not part of the initial target unless a current requirement is documented.

## Principles

- **Preserve worship operation before redesigning it.** The prepared/live split, fast verse switching, and blank-screen action are critical behavior.
- **Recover the build before refactoring.** Every later change needs a reproducible baseline.
- **Characterize before changing.** Add tests around parsing, search, rendering calculations, and file compatibility.
- **Protect user data.** Never rewrite an existing library without backup, validation, and rollback.
- **Remove scope before replacing technology.** Do not modernize unused JMF/JOGL/DirectShow code merely because it exists.
- **Deliver in small pull requests.** Each phase below has an independently reviewable exit criterion.

## Current baseline

| Concern | Current implementation | Modernization implication |
| --- | --- | --- |
| UI | Swing with FlatLaf light/dark themes and a prepared/live operator shell | Continue incremental panel and controller improvements without a framework rewrite |
| Runtime | Java 21 | Validate on real supported desktops and projector configurations |
| Build | Maven Wrapper and pull-request CI | Keep dependencies and workflow actions pinned and reviewed |
| Core rendering | Java2D in `Screen`/`ScreenViewSwing` | Retain and characterize before visual changes |
| Songs | `.txt` import plus Java-serialized `.sng` writes | Add a versioned UTF-8 format and safe migration |
| Settings | Java serialization and editable language file | Introduce explicit schema/defaults later |
| Dependencies | obsolete JMF, JOGL 1, DSJ, JGit, AppBundler | Remove the inactive DSJ/DirectShow cluster first; separate other out-of-scope dependencies incrementally |
| Tests | parser, theme/component, source-startup, and packaged-startup checks | Expand characterization around storage, rendering, and presentation state |
| Packaging | self-contained `jpackage` archives for Linux, Windows, and macOS | Add signing/notarization and upgrade validation when credentials are available |
| License/governance | no root license or contribution policy | Confirm ownership and choose a license before redistribution |

## Verified recovery evidence

Docker probes against the unchanged `dev` commit and disposable diagnostic copies narrowed the immediate build problem:

- unchanged source fails before compilation on the unresolved DSJ 0.8.64 dependency;
- removing only the dependency is insufficient because inactive multimedia sources reference DSJ types;
- excluding that DSJ/DirectShow source cluster allows the remaining project to package on JDK 8;
- the same reduced source set compiles on JDK 21 with source/target 17;
- the operator application remains alive in a 15-second Xvfb smoke launch on JDK 21;
- modern-JDK packaging then fails independently in AppBundler 1.0.4.

See [Build investigation](build-investigation.md) for the exact matrix and limitations. This evidence favors a direct Java 21 baseline and two independent changes: build recovery first, packaging replacement later.

## Phase 0 — recover a reproducible build

**Outcome:** a clean checkout compiles and tests in CI with documented commands.

Work:

1. Confirm project ownership, intended open-source license, and redistribution rights for copied/third-party source and resources.
2. Decide the supported desktop platforms and adopt Java 21 as the initial supported baseline; keep real desktop and projector validation as an exit criterion.
3. Add Maven Wrapper and pin plugin versions.
4. Remove or exclude the inactive DSJ/DirectShow source cluster and its dependency as one coherent change.
5. Remove obsolete repositories not required by the remaining build; inventory JMF/JOGL/effects separately instead of coupling all multimedia removal to the first green build.
6. Exclude the incomplete `songrepo/GitHelper` and old JGit dependency unless song synchronization becomes a current requirement.
7. Remove AppBundler from the normal lifecycle so compile/test can be green independently of installer work.
8. Add CI for compile and tests on Java 21; add a separate packaging job only after compilation is stable.
9. Document one supported local build/run command.

Exit criteria:

- `./mvnw verify` succeeds from a clean checkout;
- CI runs the same command on every pull request to `master`;
- an automated smoke check launches the operator UI, followed by a manual launch/projector check on each supported OS;
- no unverified binary is introduced to make the build green.

## Phase 1 — establish regression safety

**Outcome:** core lyrics behavior can be changed safely without a projector or interactive UI test for every edit.

Add characterization tests for:

- `.txt` parsing and `@` verse boundaries;
- existing `.sng` compatibility using sanitized fixtures;
- title/author rendering and search ranking;
- multiple-verse joining and blank selection;
- `Screen.copyFrom` text/background boundaries;
- text layout calculations with deterministic fonts where practical;
- application-data path selection through an injectable path provider.

Introduce a small manual smoke-test checklist for dual-display/fullscreen behavior. Run Swing tests headlessly where possible and keep projector checks explicit rather than pretending CI can validate hardware behavior.

Exit criteria:

- tests cover the core song/search/prepared-live workflow;
- a fixture policy explains provenance and removes real congregation data;
- rendering or persistence pull requests cannot merge without relevant tests.

## Phase 2 — protect and modernize song storage

**Outcome:** songs use an explicit, versioned, UTF-8 format without losing existing libraries.

Work:

1. Define a documented schema (for example versioned JSON) containing ID, title, alternate title, author, and ordered verse blocks.
2. Separate `SongRepository` and format codecs from the `Song` model and Swing UI.
3. Keep `.txt` import with explicit UTF-8 and documented separator escaping.
4. Implement a read-only legacy `.sng` importer in a constrained migration path; do not deserialize untrusted files during normal operation.
5. Before migration, create a timestamped backup and write new files atomically.
6. Produce a migration report with imported, skipped, duplicate, and failed files.
7. Test round trips, diacritics, duplicate titles, empty verses, and interrupted writes.

Exit criteria:

- a copy of an existing library can be migrated and rolled back;
- the new format is human-inspectable and schema-versioned;
- normal startup no longer requires Java deserialization for songs.

## Phase 3 — separate the lyrics core from Swing

**Outcome:** business behavior is testable and the operator UI becomes an adapter.

Extract incrementally:

- `SongRepository` — list, load, save, and import songs;
- `SongSearchService` — deterministic search and ranking;
- `PresentationController` — selected song/verses, prepared state, live commit, and blank action;
- `DisplayOutput` — preview and projector abstraction;
- `SettingsRepository` — typed settings with defaults and validation.

Keep Swing and `ScreenViewSwing` as the first adapters. Replace direct access to `App` fields and stringly typed property events only as each tested service is introduced.

Exit criteria:

- the core workflow runs in unit tests without constructing `App`/`JFrame`;
- `App` acts primarily as a composition root;
- presentation state has one owner and explicit transitions.

## Phase 4 — current desktop runtime and packaging

**Outcome:** contributors and operators can install a supported application artifact.

Work:

1. Validate Swing/Java2D on the selected LTS JDK and supported display configurations.
2. Replace removed/internal APIs and fix warnings before enabling strict compiler checks.
3. Use `jpackage` (or another maintained, documented packager) for native installers/runtime images.
4. Add application version metadata, icons, checksums, and release notes.
5. Define where logs and crash diagnostics are written without exposing song content by default.
6. Test upgrade/uninstall behavior without deleting user libraries.

Exit criteria:

- CI produces versioned artifacts for supported platforms;
- a fresh machine can install, launch, and display sample lyrics;
- upgrades preserve songs and settings.

## Phase 5 — operator-focused UX improvements

**Outcome:** the application is safer and faster during a live meeting.

Prioritize verified operator needs:

- an unmistakable PREVIEW versus LIVE state;
- configurable shortcuts with visible hints;
- immediate black/blank action;
- projector/display health and resolution indicator;
- autosave or clear unsaved-change warnings in the song editor;
- undo/redo and safe recovery;
- accessible font scaling and keyboard-only operation;
- a built-in rehearsal/test mode that does not require a second display.

FlatLaf is the selected visual foundation for the existing Swing UI. Modernize panels and extract testable controllers incrementally; evaluate a different toolkit only after the core is separated and measurable limitations of Swing are documented.

## Phase 6 — optional capabilities

Consider only after the lyrics core is stable:

- opt-in song library synchronization with conflict handling;
- import/export formats used by other worship tools;
- set lists and service plans;
- remote operator controls;
- background images or videos through maintained APIs.

Each optional feature needs a user story, privacy/security assessment, offline behavior, and a way to disable it.

## Suggested pull-request sequence

1. Build inventory, license decision, and supported-platform decision.
2. Remove the inactive DSJ/DirectShow cluster and its dependency/repository.
3. Adopt Java 21, add Maven Wrapper and CI, and detach AppBundler from the normal lifecycle.
4. Add the test harness, one song-parser characterization test, and a smoke-launch check.
5. Inventory and remove/isolate remaining out-of-scope multimedia and song-sync dependencies.
6. Expand `Song` text parser and search characterization tests.
7. Add prepared/live controller characterization tests.
8. Introduce `SongRepository` with the current storage adapter.
9. Add a versioned UTF-8 song format and migration tool.
10. Extract `PresentationController`.
11. Add current-JDK packaging and installers.
12. Deliver small, tested operator UX improvements.

## Decisions required before implementation

Record these as short architecture decisions in `docs/decisions/`:

- supported operating systems;
- Java LTS version;
- project and bundled-resource licensing;
- lyrics-only treatment of multimedia source;
- new song schema and legacy import lifetime;
- packaging and update strategy;
- whether song synchronization is a product requirement.

These decisions intentionally precede a large refactor. They prevent contributors from modernizing incompatible assumptions in parallel.
