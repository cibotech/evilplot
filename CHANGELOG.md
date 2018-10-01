# Changelog

## [Changes since last release]

## [0.5.x] - 2018-??-??
- added a low priority default Theme so this cumbersome import is only necessary when desired

## [0.5.0] - 2018-09-21
### Added
- `ComponentGroup` for combining plot components
- RGBA extractors on colors
- Cumulative and density binning for histograms
- Ability to display a box plot with a mean line
- Support for rendering plots in Jupyter Scala
- `displayPlot` now remembers the last save directory.
- Support for clustered box plots.
- Option to display the data's mean as a dashed line on box plots.
- `MultilineText` utility to respect newlines in text.
# Changed
- `BoxPlotRenderer` now takes `clusterSpacing` parameter (_breaking_)
-  Broken `fixedBounds` option no longer on `continuousAxis` (_breaking_)
### Fixed
- Bug in the spacing and sizing of bars in clustered bar charts.


## [0.4.1] - 2018-07-26
### Added
- Ability to partially update the bounds of a plot using `xbounds` and `ybounds`. Providing only `lower` or `upper` to these functions is now possible.
- `BoxRenderer.tufte`, for an Edward Tufte style box plot.
- New legend context builders.

### Fixed
- Bug resulting in misalignment of axes and gridlines in some faceted plots.

## [0.4.0] - 2018-07-18
### Added
- `BoxRenderer.colorBy` box renderer for custom coloring on box plots.
### Changed
- `Theme`, `Fonts`, `Colors`, and `Elements` have been changed from traits to case classes. The `DefaultTheme`, `DefaultFonts`, `DefaultColors` and `DefaultElements` types have been removed. The default theme is now available as an instance of `Theme` called `DefaultTheme`. This change is intended to make it easier to modify small parts of a theme, without having to first ensure the theme is indeed an instance of `DefaultTheme`.
- Plot renderers have been made public.
- The `renderer` and `components` fields on `Plot` are now public.

## [0.3.4] - 2018-07-13
### Added
- Axes can now be added to any side of a plot.
- Axes can use arbitrary tick bounds and be added to plots without impacting the plot bounds (for e.g. plotting data
on two different scales).
- `discreteAxis` and `continuousAxis` implicit functions added for adding custom axes.
- We now publish a new artifact, `evilplot-repl`, containing a `displayPlot` utility to assist in using EvilPlot from the Scala REPL.
- `MultiBoundsOverlay` allows creating plots with multiple layers having distinct bounds as well as the use of secondary axes.

### Changed
- Axes implicit functions updated to allow more control over axes.
- `com.cibo.evilplot.colors.GradientUtils` is now public.

### Fixed
- Regression in multistop gradients from `0.3.1`. Multi gradients were previously truncated to only use the first two colors as endpoints. This has been fixed.


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

[Changes since last release]: https://github.com/cibotech/evilplot/compare/v0.5.0...HEAD
[0.5.0]: https://github.com/cibotech/evilplot/compare/v0.4.1...v0.5.0
[0.4.1]: https://github.com/cibotech/evilplot/compare/v0.4.0...v0.4.1
[0.4.0]: https://github.com/cibotech/evilplot/compare/v0.3.4...v0.4.0
[0.3.4]: https://github.com/cibotech/evilplot/compare/v0.3.3...v0.3.4
[0.3.3]: https://github.com/cibotech/evilplot/compare/v0.3.2...v0.3.3
[0.3.2]: https://github.com/cibotech/evilplot/compare/v0.3.1...v0.3.2
[0.3.1]: https://github.com/cibotech/evilplot/compare/v0.3.0...v0.3.1
[0.3.0]: https://github.com/cibotech/evilplot/compare/v0.2.1...v0.3.0
