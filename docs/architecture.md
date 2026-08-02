# Architecture

## System context

jWorship is a local desktop application. The operator window manages songs and a prepared preview; a second display can show the live `Screen`. User data is stored on the same machine.

```text
                    edits/searches/selects
Operator ─────────────────────────────────────────┐
                                                  v
                                           Swing operator UI
                                                  |
                       prepared Screen -----------+---- commit/go
                                                  v
                                            live Screen
                                                  |
                                  ScreenViewSwing / projector
                                                  v
                                             Audience

User data directory <──── Song + settings persistence ────> App
```

There is no server, database, or network service in the main lyrics workflow. `songrepo/GitHelper` is a separate, incomplete experiment for cloning a song repository and is not called by the main application.

## Main runtime flow

1. `App.main` constructs the Swing application on the Event Dispatch Thread.
2. `App` creates the per-user directories, loads settings/translations, loads songs, and assembles operator panels.
3. `SongsPanel` displays `App.songs` through `ObjectListModel`, searches `Song.getSearchInfo()`, and displays the selected song's verses.
4. Selecting verses updates a prepared `Screen` with an `AttributedString`.
5. `AppPanel.go(...)` copies the selected portion of the prepared screen to the live screen.
6. `ScreenViewSwing` renders the live screen in a window or fullscreen on a graphics device.
7. `Screen.paint(...)` paints the background and lays out outlined white lyrics using Java2D.

The prepared/live split is an important behavioral boundary: an operator can compose or preview content before making it visible to the audience.

## Components

### Application coordinator — `App`

`sk.calvary.worship.App` currently owns most runtime state and orchestration:

- application and projector windows;
- user-data directories;
- songs and list models;
- prepared and live `Screen` objects;
- operator panels and transitions;
- settings, language, and bookmarks;
- media lookup and update dispatch.

It is both a `JFrame` and the composition root. This makes initialization and domain behavior difficult to test without Swing. The modernization plan separates services behind the existing UI instead of replacing the UI first.

### Song domain and persistence — `Song`, `SongEditor`

`Song` holds title, alternate title, author, and a `Vector<String>` of verses. It also implements both parsing and persistence:

- `.txt` import with `@` separators;
- `.sng` Java deserialization;
- `.sng` Java serialization on save;
- creation of search text.

`SongEditor` binds Swing fields directly to `Song` through `DialogAssist` and saves into the application's songs directory.

### Operator lyrics UI — `SongsPanel`

`SongsPanel` combines presentation, search ranking, selection state, keyboard commands, song editing, formatting controls, and prepared-screen updates. It communicates with `App` through direct references and JavaBeans property-change events.

### Rendering model — `Screen`

`Screen` describes what will be painted:

- attributed lyrics text;
- text alignment and screen region;
- wrapping, fit, shadow, width, height, and font size;
- optional background media.

Its `paint` method uses Java2D text measurement and path outlines. `copyFrom` can copy text, background, or both, supporting the prepared-to-live workflow.

### Display adapters — `ScreenView`, `ScreenViewSwing`

`ScreenView` is the display abstraction. `ScreenViewSwing` is the main Java2D implementation for preview/fullscreen output and display selection. `ScreenViewJogl` is a legacy OpenGL implementation tied to JOGL 1.

### Panels and optional media

`AppPanel` is the common base for operator tabs. `BackPicPanel` handles image backgrounds. Video, capture, effects, JMF, JOGL, and DirectShow packages introduce most obsolete external dependencies. The main `App` currently leaves multimedia panel creation commented out, so these packages should not block a lyrics-only core build.

### Utilities

`sk.calvary.misc` contains string/search helpers, graphics helpers, language parsing, and Swing models. Several are reusable but currently lack unit tests and clear API boundaries.

## State and data flow

```text
.txt/.sng files
      |
      v
 Song.load ──> App.songs/ObjectListModel ──> SongsPanel search/selection
                                                     |
                                                     v
                                               prepared Screen
                                                     |
                                              AppPanel.go/copyFrom
                                                     v
                                                 live Screen
                                                     |
                                                     v
                                         ScreenViewSwing + Java2D
```

Settings and bookmarks are serialized separately under the user settings directory. Translation data starts from the bundled `sk/calvary/misc/lang.lng` resource and is copied into the user settings directory for customization.

## Architectural risks

1. **UI and domain coupling:** `App` and `SongsPanel` own behavior that cannot be tested headlessly.
2. **Unsafe, opaque persistence:** Java serialization is used for songs and settings.
3. **Unclear dependency scope:** disabled multimedia source is compiled with the lyrics application.
4. **Mutable shared state:** `Vector`, direct object references, and property-name strings coordinate UI state.
5. **Platform assumptions:** fullscreen, paths, font name, and charset behavior vary by operating system.
6. **No regression safety net:** rendering, parsing, search, and storage behavior have no automated tests.

The goal is not a big-bang rewrite. Preserve the prepared/live operating model and move testable behavior behind explicit interfaces one slice at a time.
