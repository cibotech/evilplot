# Changelog

## [Changes since last release]
### Added
- Axes can now be added to any side of a plot.
- Axes can use arbitrary tick bounds and be added to plots without impacting the plot bounds (for e.g. plotting data
on two different scales).
- `discreteAxis` and `continuousAxis` implicit functions added for adding custom axes.

### Changed
- Axes implicit functions updated to allow more control over axes.

## [0.3.3] - 2018-07-02
Artifacts for both Scala 2.11 and Scala 2.12 are published as a part of this release. There are no changes in functionality.

## [0.3.2] - 2018-06-28
### Added
- `BarRenderer.named`, which allows specifying a name and color to the legend context,
especially in histograms.

## [0.3.1] - 2018-06-14
### Added
- Ability to create data positioned plot components.
- Support for gradients clipping colors at the edges.

### Fixed
- Axis components that exceed size of a border plot no longer affect positioning of the entire border plot.
- Incorrect x transformation used for `leftPlot`s has been corrected.

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

[Changes since last release]: https://github.com/cibotech/evilplot/compare/v0.3.3...HEAD
[0.3.3]: https://github.com/cibotech/evilplot/compare/v0.3.2...v0.3.3
[0.3.2]: https://github.com/cibotech/evilplot/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/cibotech/evilplot/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/cibotech/evilplot/compare/v0.2.1...v0.3.0
