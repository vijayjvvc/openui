# Changelog

All notable changes to this project are documented here.
The format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/)
and the project uses [Semantic Versioning](https://semver.org/).

## [Unreleased]

## [1.0.0] - 2026-10-12

### Added
- `OceanWaveView`: rounded card with a live animated ocean wave, outer border,
  bordered icon circle (custom drawable, tint, border color and width) and a
  TextView-like label (max lines, ellipsize, alignment, line spacing, letter
  spacing, font family, uppercase, position).
- `MoreAppsView`: "More apps" list loaded from a JSON API. The last response is
  cached in SharedPreferences and shown instantly while the API is called again.
  Includes a loading skeleton, a built-in icon loader (memory + disk cache) and
  XML-only options such as `listCount`.
- `RowanUi.setDebug(boolean)` for Logcat debug output (tag `RowanUi`).

[Unreleased]: https://github.com/rowanlabs/openui/compare/1.0.0...HEAD
[1.0.0]: https://github.com/rowanlabs/openui/releases/tag/1.0.0