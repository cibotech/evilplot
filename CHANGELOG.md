# Changelog

## [Changes since last release]

## [0.3.0] - 2018-05-23
### Added
- Ability to add custom components to plots with `component`

### Fixed
- `Path.apply` and `Polygon.apply` translate negative coordinates to positive,
correcting previously incorrect extent calculation.
- Improved tick labeling for fixed bounds plots.

### Changed
- Applying "bound buffers" to plots is no longer part of the default theme.
- The default number of ticks on continuous axes has been decreased.

[Changes since last release]: https://github.com/cibotech/evilplot/compare/v0.3.0...HEAD
[0.3.0]: https://github.com/cibotech/evilplot/compare/v0.2.1...v0.3.0
