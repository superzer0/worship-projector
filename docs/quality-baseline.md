# Quality baseline

The first measured baseline was recorded on `master` commit `0b94c56` before persistence-safety work:

- 75 production Java files;
- approximately 10,255 nonblank/non-comment production lines;
- 6 test classes and 13 JUnit tests;
- JaCoCo: 3.4% lines and 2.1% branches.

After adding compatibility, atomic persistence, and deserialization tests in this branch, the measured baseline is:

- 35 JUnit tests, with the display-dependent test skipped in a headless run and executed separately under Xvfb;
- JaCoCo: 7.4% lines, 6.0% branches, and 9.1% methods.

`mvn verify` now:

1. enforces Java 21 and Maven 3.9.16 or newer;
2. checks dependency convergence;
3. fails on dependency-analysis warnings;
4. produces `target/site/jacoco/`;
5. enforces focused line and branch coverage floors for the new atomic-write and deserialization-filter classes and the changed song/serialized-settings paths.

The repository-wide percentage is intentionally recorded rather than enforced at an unrealistic level. New persistence and controller code must have focused tests, and coverage enforcement should move package-by-package as legacy seams are extracted.
