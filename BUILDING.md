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

This is the command used by continuous integration. It compiles the Java 21 source, runs the characterization tests, and creates `target/worship-1.0-SNAPSHOT.jar`.

## Run from the build tree

Prepare runtime dependencies:

```bash
./mvnw dependency:copy-dependencies \
  -DincludeScope=runtime \
  -DoutputDirectory=target/dependency
```

Then start the operator application:

```bash
java -cp 'target/classes:target/dependency/*' sk.calvary.worship.App
```

On Windows, replace `:` in the classpath with `;`.

The generated JAR is not yet a self-contained distribution. Replacing the obsolete macOS AppBundler with maintained packaging is intentionally a separate modernization step.

## Linux smoke test

Install `xvfb` and the X11 client libraries required by Java AWT (`libxi6`, `libxrender1`, and `libxtst6` on Debian/Ubuntu), compile the application, copy its dependencies, and run:

```bash
./scripts/smoke-test.sh
```

The script creates a temporary user-data directory and verifies that the Swing operator UI stays alive for 15 seconds on a virtual display. It does not validate real projector selection, multi-display behavior, media playback, or live operator workflows.
