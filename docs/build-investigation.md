# Build investigation

This page records controlled build probes against `dev` commit `3599ab4`. The probes were run on 2 August 2026 in disposable copies of the source tree. They do not claim that the current `dev` branch build is green.

## Environment

The probes used official Maven container images on Linux:

| Image tag | Maven | Java |
| --- | --- | --- |
| `maven:3.9-eclipse-temurin-8` | 3.9.16 | Temurin 8u492 |
| `maven:3.9-eclipse-temurin-17` | 3.9.16 | Temurin 17.0.19 |
| `maven:3.9-eclipse-temurin-21` | 3.9.16 | Temurin 21.0.11 |

The tags above are convenient labels, not immutable digests. CI should pin the selected toolchain through Maven Wrapper plus an explicit JDK distribution/version, and container-based jobs should pin an image digest.

## Results

| Probe | Result | What it establishes |
| --- | --- | --- |
| Unchanged `dev`, JDK 8, `mvn -U -B -DskipTests compile` | **Failed during dependency resolution** | `de.humatic.dsj:dsj:0.8.64` cannot be resolved through `https://maven.java.net/...`; Maven reports PKIX certificate validation failure |
| Unchanged `dev`, JDK 17, `mvn -B -DskipTests compile` | **Failed during dependency resolution** | A newer JDK does not bypass the obsolete dependency repository |
| Remove only the DSJ dependency, JDK 8, `mvn compile` | **Failed during compilation** | DSJ types are referenced by the vendored `de/humatic` adapter sources and `MultimediaChannel`; the dependency is not unused from Maven's point of view |
| Exclude the inactive DSJ/DirectShow source cluster and dependency, JDK 8, `mvn clean package` | **Succeeded** | The remainder of the historical project can still compile and package on its original-era JDK |
| Same reduced source set, source/target 1.8, JDK 17, `mvn clean test` | **Succeeded; no tests found** | The Java source compiles on JDK 17 once the immediate DSJ blocker and unsupported Java 6 target are removed |
| Same reduced source set, source/target 17, JDK 21, `mvn clean test` | **Succeeded; no tests found** | A direct Java 21 recovery path is technically plausible; stepping through every intermediate Java release is not required for compilation |
| JDK 17 or 21, `mvn clean package` | **Failed after JAR creation** | `appbundle-maven-plugin:1.0.4` reports an API incompatibility/`ExceptionInInitializerError`; compilation and JAR creation had already completed |
| JDK 21 build, dependencies copied, `App -testmode` under Xvfb | **Stayed alive for a 15-second smoke window** | The operator application reaches and remains in the Swing event loop in a virtual single-display environment; this is not a functional or projector test |

## Temporary diagnostic scope

To isolate causes without changing the pull request's application code, the successful probes used disposable source copies with these temporary changes:

1. removed the `de.humatic.dsj:dsj:0.8.64` dependency;
2. excluded `src/main/java/de/humatic/**`;
3. excluded `MultimediaChannel.java` and `MultimediaPanel.java`, whose creation is already commented out in `App.loadPanels()`;
4. changed Maven source/target only for the JDK 17 and JDK 21 probes.

JMF, JOGL 1, JGit, the effects package, image backgrounds, and the Swing lyrics code remained present in the successful compilation probes. They are still obsolete or out of the lyrics-only scope, but they are not all required to be removed in one pull request to recover compilation.

## Conclusions

1. **The first build-recovery change can be narrow.** Remove or isolate the inactive DSJ/DirectShow cluster, remove its repository/dependency, and target Java 21.
2. **Separate compile/test from packaging.** Establish `mvn test`/`verify` first; replace AppBundler independently with a maintained packaging approach such as `jpackage`.
3. **Do not preserve DSJ with an arbitrary downloaded JAR.** The feature is inactive, outside the lyrics-only goal, and brings unverifiable binary and licensing risk.
4. **Do not infer test coverage from a green Maven test phase.** Maven explicitly reported `No tests to run`.
5. **Keep real desktop validation as an exit criterion.** Xvfb does not validate display selection, fullscreen behavior, font rendering, keyboard timing, or projector output.

## Recommended first implementation slice

A focused follow-up pull request should:

1. remove/exclude the inactive DSJ/DirectShow source and dependency;
2. remove obsolete repositories not needed by the remaining build;
3. select Java 21 and update the compiler configuration;
4. add Maven Wrapper and a Java 21 compile/test CI job;
5. disable/remove AppBundler from the normal lifecycle so `verify` is green;
6. add an empty test harness plus one small song-parser characterization test;
7. document a temporary classpath-based developer launch command;
8. leave native installers and broader multimedia removal for separate pull requests.

This sequence turns the verified probe into a reviewable baseline without mixing it with persistence migration, UI redesign, or packaging replacement.
