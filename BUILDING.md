# Building jWorship

## Requirements

- JDK 21
- `unzip` on Unix-like systems, used by the checksum-verified Maven Wrapper bootstrap
- Linux, macOS, or Windows with a graphical desktop for interactive use

The Maven Wrapper downloads the pinned Maven version, so a separate Maven installation is not required.

## Compile and test

```bash
./mvnw verify
```

On Windows Command Prompt or PowerShell, use:

```powershell
.\mvnw.cmd verify
```

This is the primary command used by pull-request continuous integration. CI also starts the Swing UI on a virtual display, builds the self-contained Linux application, extracts the resulting archive, and starts that packaged application. The release workflow additionally builds and smoke-tests the Windows x64, macOS Intel, and macOS Apple silicon archives on every pull request without creating a tag or release.

## Run from the build tree

Prepare runtime dependencies:

```bash
./mvnw dependency:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory=target/dependency
```

On Windows, invoke the same goal with `.\mvnw.cmd`.

Then start the operator application:

```bash
java -cp 'target/classes:target/dependency/*' sk.calvary.worship.App
```

On Windows, replace `:` in the classpath with `;`.

The Maven-generated JAR is not self-contained. Tagged releases use `jpackage` to bundle the application, its dependencies, and a Java 21 runtime.

## Linux smoke test

Install `xvfb` and the X11 client libraries required by Java AWT (`libxi6`, `libxrender1`, and `libxtst6` on Debian/Ubuntu), compile the application, copy its dependencies, and run:

```bash
./scripts/smoke-test.sh
```

The script creates a temporary user-data directory and verifies that the Swing operator window becomes visible and remains alive for 15 seconds on a virtual display. It does not validate real projector selection, multi-display behavior, media playback, or live operator workflows.

## Publishing a release

Every update to `master` automatically starts the release workflow. After all platform packages and checksums pass validation, the workflow creates a `v1.0.N` tag for that exact commit and publishes the corresponding GitHub release. `N` is the stable GitHub Actions run number, so rerunning a failed workflow reuses the same tag instead of creating another version. The positive major version is required by macOS `jpackage` and is shared by every platform artifact.

Every release reruns the tests, extracts and starts each platform-specific download before creating the tag, and publishes these self-contained downloads on the repository's GitHub Releases page:

- Linux x64: `jWorship-X.Y.Z-linux-x64.tar.gz`
- Windows x64: `jWorship-X.Y.Z-windows-x64.zip`
- macOS Intel: `jWorship-X.Y.Z-macos-x64.zip`
- macOS Apple silicon: `jWorship-X.Y.Z-macos-arm64.zip`

Each archive has a matching `.sha256` checksum. Users do not need to install Java. After extraction, launch `jWorship/bin/jWorship` on Linux, `jWorship/jWorship.exe` on Windows, or `jWorship.app` on macOS.

The applications are not yet code-signed. Windows SmartScreen or macOS Gatekeeper may therefore ask the user to confirm the first launch. Signing and notarization require platform signing credentials and remain a separate release-hardening step.

## UI themes

Choose the light or dark operator theme from **Appearance → Theme**. For screenshot or diagnostic runs, set the startup override `-Djworship.theme=light` or `-Djworship.theme=dark`. The preference selected in the application is saved with the existing general settings.
