# jWorship

jWorship is a Java Swing desktop application for presenting song lyrics during worship meetings. An operator can search the song library, select a song and one or more verses, preview the result, and send it to a projector or second display.

> [!IMPORTANT]
> The `dev` branch is the development baseline. It is historical Java 6-era code and is not yet reproducibly buildable on a current developer workstation. See [Development](docs/development.md) for the observed baseline and [Modernization plan](docs/modernization-plan.md) for the recovery path.

## What the application does

The lyrics workflow currently supports:

- searching songs by title and lyrics;
- creating and editing songs;
- splitting lyrics into selectable verses;
- presenting a selected verse, multiple verses, or a blank screen;
- previewing content before it is sent live;
- projecting to a second display;
- changing text alignment, fit, wrapping, shadow, and screen area;
- storing UI labels in English, Slovak, and Polish.

The repository also contains experimental or legacy image, video, JMF, JOGL, and DirectShow code. These areas are outside the initial lyrics-only modernization scope.

## Documentation

- [Development guide](docs/development.md) — repository setup, data formats, local files, and current build limitations
- [Build investigation](docs/build-investigation.md) — Docker-verified blockers and the narrowest recovery path
- [Architecture](docs/architecture.md) — runtime flow, major components, and design boundaries
- [Modernization plan](docs/modernization-plan.md) — phased, test-first route to a maintainable application
- [Contributing](CONTRIBUTING.md) — branch, change, and pull-request conventions

## Repository layout

```text
.
├── pom.xml                         Maven build descriptor
├── src/main/java/sk/calvary/misc
│   ├── lang.lng                     Bundled translations
│   └── …                            Shared utilities and UI models
├── src/main/java/sk/calvary/worship
│   ├── App.java                    Swing entry point and application coordinator
│   ├── Song.java                   Song model and persistence
│   ├── Screen.java                 Lyrics/background render model
│   ├── ScreenView*.java            Preview and projector views
│   ├── panels/                     Operator controls
│   └── effects/ and jmf/           Legacy multimedia code
└── docs/                            Contributor documentation
```

## Historical build command

The original build instructions were:

```bash
mvn install
java -jar target/worship-1.0-SNAPSHOT.jar
```

Do not treat that command as a supported modern build yet. The current Maven model targets Java 6 and references obsolete dependencies and repositories. Re-establishing a reproducible build is the first modernization milestone.

## Project direction

Modernization should preserve the reliable live-lyrics workflow before changing the UI or storage format. The first goal is a small, testable lyrics application: song library, fast operator selection, preview, and projector output. Multimedia experiments should be isolated or removed from the default build unless a current requirement and supported replacement are established.
