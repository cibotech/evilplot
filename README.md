# Evil Plot

Evil Plot is about combinators for graphics.

### Table of Contents
  * [Low-Level Drawing API](#low-level-drawing-api)
  * [Plot API](#plot-api)
    * [Annotations](#annotations)
    * [Axes](#axes)
    * [Bounds](#bounds)
    * [Grids](#grids)
    * [Labels](#labels)
    * [Legends](#legends)
    * [Lines](#lines)
    * [Padding](#padding)
  * [Custom Plots and Plot Components](#custom-plots-and-plot-components)
    * [Plot Components](#plot-components)
    * [Plot Renderers](#plot-renderers)
    * [Plot Element Renderers](#plot-element-renderers)
  * [Working on this Project](#working-on-this-project)

## Low-Level Drawing API
The low-level drawing API exists in the `com.cibo.evilplot.geometry` package.  Drawing primitives
extend the `Drawable` trait.  Once constructed, these can then be rendered using a `RenderContext`
(such as `CanvasRenderContext`, used for rendering to a JavaScript canvas or `Graphics2DRenderContext`,
used for rendering to `java.awt.Graphics2D`).

Drawing primitives can be divided into three categories: drawing, positioning, and style.
The following primitives for drawing are available:

* EmptyDrawable
* Line
* Path
* Rect
* BorderRect
* Disc
* Wedge
* Text

The following primitives for positioning are available:

* Translate
* Affine
* Scale
* Rotate
* Group
* Resize

Finally, the following primitives for style are available:

* Style
* StrokeStyle
* StrokeWeight

The `com.cibo.evilplot.geometry` package object provides several convenience functions for
creating and composing these primitives.

## Plot API
The plot API is in the `com.cibo.evilplot.plot` package. Using this API, one creates a plot
using a plot constructor.  Once a plot is constructed, it is possible to change properties
and add components to the plot.  The following plot constructors are
available (though additional constructors can be added):

* BarChart
* Heatmap
* Histogram
* LinePlot
* PieChart
* ScatterPlot
* SurfacePlot

Multiple plots can be combined using the following plot constructors:
* Facets - Creates a 2-dimensional grid of plots with aligned axes.
* Overlay - Stacks multiple plots on top of each other with aligned axes.

Plot components are available in the `com.cibo.evilplot.plot.components` package, though the
`com.cibo.evilplot.plot` package object provides implicits on the `Plot` object itself to
make them easier to use.

### Annotations
Annotations are text (or other `Drawable`s) placed on top of the plot area.
The X and Y coordinate parameters provide the relative position of the annotation
(in the range 0 to 1).

* annotate - Add an annotation

### Axes
The following methods (from the package object) are available for adjusting axes.
These take arguments to alter the type of axis.

* xAxis - Add an x-axis
* yAxis - Add a y-axis

### Bounds
By default, the bounds of the plot area are determined by the data and plot constructor.
However, it's possible to alter the bounds explicitly:

* xbounds - Set the min/max x bounds
* ybounds - Set the min/max y bounds

### Grids

* xGrid - Add vertical grid lines
* yGrid - Add horizontal grid lines

### Labels

Labels for a single plot:

* title - Add a title to the top of the plot area
* topLabel - Add a label above the plot area
* bottomLabel - Add a label below the plot area
* leftLabel - Add a label to the left of the plot area
* rightLabel -  Add a label to the right of the plot area

Labels for faceted plots:

* topLabels - Add a label for each facet above the plot area
* bottomLabels - Add a label for each facet below the plot area
* leftLabels - Add a label for each facet to the left of the plot area
* rightLabels - Add a label for each facet to the right of the plot area

### Legends

* topLegend - Add a legend above the plot area
* bottomLegend - Add a legend below the plot area
* leftLegend - Add a legend to the left of the plot area
* rightLegend - Add a legend to the right of the plot area
* overlayLegend - Overlay a legend on the plot area
* renderLegend - Return a drawable to represent the legend

### Lines

* hline - Draw a horizontal line on the plot area
* vline - Draw a vertical line on the plot area
* trend - Draw a trend line on the plot area
* function - Draw a function on the plot area

### Padding
Add padding around a plot area. This is mostly used for lining up plot areas of
multiple plots in facets.

* padTop
* padBottom
* padLeft
* padRight

## Custom Plots and Plot Components
Custom plots can be constructed by creating new plot components, plot renderers, and/or
plot element renderers.

### Plot Components
A plot component is an object that goes around a plot area (such as a label or axis),
in front of it (such as a trend line), or behind it (such as a background).
Custom plot components can be implemented by extending
`com.cibo.evilplot.plot.comonents.PlotComponent`
(or `com.cibo.evilplot.plot.components.FacetedPlotComponent` for components that need to
render differently for each facet).

To add a custom component to a plot (or any component), use either the `:+` or `+:` method on
`Plot`, which will return an updated plot with the component.  `plot :+ component` will
insert the component furthest away from the plot area and `component +: plot` will insert the
component closest to the plot area.

### Plot Renderers
Custom plot renderers are created by extending
`com.cibo.evilplot.plot.renderers.PlotRenderer`.  Instances of these can then be used as an
argument to `Plot` to create custom plots.

### Plot Element Renderers
A plot element is an instance of `com.cibo.evilplot.plot.renderers.PlotElementRenderer`.  These
are used by `PlotRenderer` instances to render elements of a plot.  For example, the
`PlotRenderer` used for `ScatterPlot` uses a `PointRenderer` to render points.  It is not necessary
that a `PlotRender` use `PlotElementRenderer`s, but doing so allows them to share common code.

The following `PlotElementRenderer`s are provided:

* BarRenderer
* BoxRenderer
* PathRenderer
* PointRenderer
* SurfaceRenderer

## Working on this Project
One-time setup:
Install PhantomJS: On macOS, this is available from Homebrew: `brew install phantomjs` 

To run the demo:

1. open sbt console
2. compile, then run fastOptJS listening for changes
```bash
$ sbt
> compile
> ~ fastOptJS
```
3. go to `localhost:12345/index.html` in your browser

Running the unit tests requires PhantomJS.
Unit tests must be run from `sbt`, not from within IntelliJ.
