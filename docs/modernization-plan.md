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
| UI | Java Swing with generated/hand-written layouts | Keep initially; decouple behavior from widgets |
| Runtime | Java 6 source/target in Maven | Move through a compiling compatibility baseline to a current LTS JDK |
| Build | One old `pom.xml`, no wrapper or CI | Pin Maven/JDK and add CI first |
| Core rendering | Java2D in `Screen`/`ScreenViewSwing` | Retain and characterize before visual changes |
| Songs | `.txt` import plus Java-serialized `.sng` writes | Add a versioned UTF-8 format and safe migration |
| Settings | Java serialization and editable language file | Introduce explicit schema/defaults later |
| Dependencies | obsolete JMF, JOGL 1, DSJ, JGit, AppBundler | Separate or remove from default lyrics build |
| Tests | none | Add a headless characterization suite |
| Packaging | executable JAR plus old macOS bundle plugin | Replace after the core build is green |
| License/governance | no root license or contribution policy | Confirm ownership and choose a license before redistribution |

## Phase 0 — recover a reproducible build

**Outcome:** a clean checkout compiles and tests in CI with documented commands.

Work:

1. Confirm project ownership, intended open-source license, and redistribution rights for copied/third-party source and resources.
2. Decide the supported desktop platforms and select a current LTS Java target (prefer Java 21 if desktop validation succeeds).
3. Add Maven Wrapper and pin plugin versions.
4. Create a lyrics-only default source set/module that does not compile JMF, JOGL 1, DSJ, or effects code.
5. Remove obsolete HTTP repositories and dependencies from the default path.
6. Fix source-level blockers such as the malformed identifier in `songrepo/GitHelper.java`, or exclude the incomplete helper pending a requirement.
7. Add CI for compile and tests on the chosen JDK; add a separate packaging job only after compilation is stable.
8. Document one supported local build/run command.

Exit criteria:

- `./mvnw verify` succeeds from a clean checkout;
- CI runs the same command on every pull request to `dev`;
- the application launches into the operator UI on each supported OS;
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

Avoid a framework rewrite as the first UX task. Evaluate JavaFX or another UI only after the core is separated and measurable limitations of Swing are documented.

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
2. Lyrics-only compiling module/source set plus Maven Wrapper.
3. CI with an empty test harness and smoke launch check.
4. `Song` text parser and search characterization tests.
5. Prepared/live controller characterization tests.
6. `SongRepository` interface with current storage adapter.
7. Versioned UTF-8 song format and migration tool.
8. `PresentationController` extraction.
9. Current-JDK packaging and installers.
10. Small, tested operator UX improvements.

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
