# Contributing to jWorship

## Development branch

Use `master` as the base branch for development work and target pull requests to `master`.

```bash
git clone --branch master https://github.com/superzer0/worship-projector.git
cd worship-projector
git switch -c <type>/<short-description>
```

Suggested branch prefixes are `docs/`, `fix/`, `test/`, `refactor/`, and `feature/`.

## Before changing behavior

jWorship has a Java 21 build, characterization tests, and application startup checks. For every behavior change:

1. keep changes small and focused;
2. document the behavior being preserved;
3. add a characterization test where the changed code can be isolated;
4. do not rewrite song files or settings without a backup and compatibility test;
5. test projector behavior on a real two-display setup for rendering changes;
6. report build or runtime limitations honestly in the pull request.

## Scope

The initial modernization target is lyrics presentation: library, search, editing, preview, and projector output. Avoid coupling foundational work to the legacy multimedia packages (`jmf`, `effects`, DirectShow, and JOGL). A dependency should remain in the default application only when a current user requirement justifies it.

## Pull requests

A pull request should include:

- the problem and intended user outcome;
- the files or subsystem changed;
- tests and manual checks performed;
- compatibility impact on `.sng`, `.txt`, and settings files;
- screenshots for visible UI changes;
- known limitations or follow-up work.

Prefer one modernization milestone or independently reviewable slice per pull request. Do not combine build recovery, storage migration, architectural refactoring, and a visual redesign in one change.

## Commit style

Use a short imperative subject, for example:

```text
docs: describe the prepared-to-live lyrics flow
test: characterize text song parsing
build: establish a supported Java toolchain
```
