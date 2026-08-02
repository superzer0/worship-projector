# ADR 0001: Retain Swing and adopt FlatLaf

## Status

Accepted — 2 August 2026

## Context

The operator UI, keyboard actions, prepared/live workflow, settings, and projector coordination are implemented directly in Swing. Replacing Swing with JavaFX, Compose Desktop, or a browser shell would require a broad rewrite before the presentation core is separated and fully characterized.

The historical platform look and feel also makes the application appear dated, scales poorly in dense controls, and provides weak visual separation between prepared and live content.

## Decision

Retain Swing and Java2D. Use FlatLaf as the application look and feel, with supported light and dark themes. Introduce modern operator components and semantic colors incrementally while preserving the existing panel and action implementations.

Projection rendering remains outside look-and-feel styling. `Screen` and `ScreenViewSwing` continue to control the audience output explicitly.

## Consequences

- Existing Swing panels and keyboard behavior can be reused.
- The application gains current control styling and HiDPI support with one runtime dependency.
- Panel layout and application-state coupling still require incremental refactoring; a look and feel cannot solve those concerns by itself.
- FlatLaf upgrades become part of dependency and visual-regression review.
- A different UI toolkit should be reconsidered only when a tested presentation core exists and concrete Swing limitations justify migration.
