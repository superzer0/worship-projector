# Development guide

## Baseline

The `dev` branch is the only development baseline covered by this guide. At the time this documentation was written, the project had:

- one Maven module producing `worship-1.0-SNAPSHOT.jar`;
- main class `sk.calvary.worship.App`;
- compiler source and target set to Java 6;
- no Maven Wrapper and no CI configuration;
- no `src/test` test suite or test dependency;
- legacy JMF 2.1.1e, JOGL 1.1.1, DirectShow/DSJ, JGit 3.4, and macOS AppBundler references;
- dependencies hosted partly in obsolete HTTP repositories.

This means the historical `mvn install` command is a reference, not a reliable onboarding path. The first milestone in the [modernization plan](modernization-plan.md) establishes a pinned toolchain and a green CI build.

## Getting the source

```bash
git clone --branch dev https://github.com/superzer0/jWorship.git
cd jWorship
git switch -c <type>/<short-description>
```

All development pull requests should target `dev`.

## Historical build and launch

The Maven descriptor declares an executable JAR with `sk.calvary.worship.App` as the entry point:

```bash
mvn package
java -jar target/worship-1.0-SNAPSHOT.jar
```

If the manifest/classpath assembly fails, the historical fallback was:

```bash
java -cp target/worship-1.0-SNAPSHOT.jar sk.calvary.worship.App
```

These commands currently fail before compilation because the old dependencies are no longer reliably resolvable. A clean Docker verification with Maven 3.9.16 and Temurin JDK 8 failed while resolving `de.humatic.dsj:dsj:0.8.64` through the legacy `maven.java.net` repository (`PKIX` certificate validation failure). The same unresolved dependency blocks an unchanged build on JDK 17. Using an older or newer JDK alone therefore does not provide a reproducible build.

Do not solve that by downloading unverified JAR files. Inventory, license-check, hash, and document any binary that must temporarily be preserved.

See [Build investigation](build-investigation.md) for the full Docker verification matrix. It demonstrates that excluding the inactive DSJ/DirectShow source cluster allows the remaining code to compile on Java 21; packaging then fails independently in the obsolete AppBundler plugin.

## Runtime data

`App.getApplicationDataFolderPath()` selects a per-user `jWorship` directory:

| Platform | Current location |
| --- | --- |
| Windows | `%AppData%/jWorship/` |
| macOS | `~/Library/Application Support/jWorship/` |
| Linux/other | `~/jWorship/` |

On startup the application creates these subdirectories:

```text
jWorship/
├── pictures/
├── songs/
├── videos/
└── settings/
```

The settings directory contains serialized application state and a writable copy of `lang.lng`. Treat the whole directory as user data and back it up before testing persistence changes.

## Song formats

`Song.load(File)` accepts two extensions:

- `.txt` — plain text; the filename becomes the title and `@` separates verses while loading;
- `.sng` — Java `ObjectInputStream` serialization of `sk.calvary.worship.Song`.

The editor's plain-text representation separates verses with a line containing `@` (internally `\n@`). New or edited songs are currently saved as `.sng` files with a filename derived from the lower-case title.

Example text song:

```text
First verse line 1
First verse line 2
@
Chorus line 1
Chorus line 2
```

Cautions:

- `.sng` ties user data to the Java class and serial version;
- loading Java serialization from an untrusted source is unsafe;
- `FileReader` uses the platform default charset for `.txt` files;
- format migration must retain an importer and create backups before rewriting files.

## Operator flow useful for manual testing

1. Launch the application with at least one song in the user `songs/` directory.
2. Search by title or lyric text.
3. Select a song and verify its verses appear.
4. Select one or more verses and verify the prepared preview.
5. Send the prepared screen live.
6. Clear the selection and verify a blank lyrics screen.
7. With a second display connected, verify projector selection/fullscreen behavior.
8. Create or edit a song, restart, and verify it loads without data loss.

Keyboard behavior:

- `F5` sends the complete prepared screen live;
- `Shift+F5` copies the live screen back to the prepared screen;
- `Ctrl+F` focuses the song search field;
- `Ctrl+S` saves general settings and picture bookmark/history state;
- `1` through `9` in the song panel select and send the corresponding verse;
- `0` in the song panel clears the verse selection and sends blank lyrics;
- Up/Down in the search field changes the selected song.

Song changes are saved from `SongEditor`; general settings and picture state are saved by `Ctrl+S` and again when the application window closes, rather than continuously.

## Debugging boundaries

Start with the lyrics path and avoid initializing the legacy multimedia code:

```text
App → SongsPanel → Song → Screen → ScreenViewSwing
```

Rendering changes require both a normal preview check and a real multi-display check. Persistence changes require fixtures representing existing `.txt` and `.sng` files. Swing UI updates must run on the Event Dispatch Thread.

## Known onboarding blockers

| Area | Evidence in `dev` | Effect |
| --- | --- | --- |
| Java level | `pom.xml` uses source/target `1.6`; a controlled source/target 17 probe compiled on JDK 21 | Current JDKs reject the configured Java 6 target, but a direct move to Java 21 is plausible |
| Dependency resolution | DSJ 0.8.64 is fetched through legacy `maven.java.net`; removing only the dependency exposes DSJ references in inactive multimedia source | A clean build fails until the DSJ/DirectShow source cluster and dependency are removed or isolated together |
| Packaging | AppBundler 1.0.4 succeeds in the reduced JDK 8 probe but fails with an API incompatibility on JDK 17/21 after JAR creation | Compile/test recovery and packaging replacement should be separate changes |
| Tests | no `src/test` tree | Changes cannot be regression-checked automatically |
| Song repository helper | `songrepo/GitHelper.java` is a standalone experiment with hard-coded paths and repository URL | It is outside the main flow and keeps old JGit in the dependency graph without a current product requirement |
| Persistence | Java serialization and platform-default text encoding | Compatibility and security risk |

Keep this table synchronized with verified build results as milestones are completed.
