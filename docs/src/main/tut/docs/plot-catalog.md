---
layout: docs
title: Plot Catalog
---
# Plot Catalog

This is the Plot Catalog, where we've assembled some interesting EvilPlots along with the code to produce them.

## Using Emoji

## Tufte style box plot
<!-- Inspired by Edward Tufte's _The Visual Display of Quantitative Information_. We define a custom `BoxRenderer`:
```scala
def tufteLikeBoxRenderer(implicit theme: Theme) = new BoxRenderer {
	def render(plot: Plot, extent: Extent, summary: BoxPlotSummaryStatistics): Drawable = {
		import summary._
		val scale = extent.height / (upperWhisker - lowerWhisker)
		// heights
		val topWhisker = upperWhisker - upperQuantile
		val bottomWhisker = lowerQuantile - lowerWhisker
		val upperToMiddle = upperQuantile - middleQuantile
		val middleToLower = middleQuantile - lowerQuantile
		val strokeWidth = theme.elements.strokeWidth

		Align.center(
		  Line(scale * topWhisker, strokeWidth).rotated(90),
		  Disc.centered(theme.elements.pointSize)
		  	.padTop(scale * upperToMiddle)
		  	.padBottom(scale * middleToLower),
		  Line(scale * bottomWhisker, strokeWidth).rotated(90)
		).reduce(_ above _)
		 .colored(theme.colors.path)
	}
}
```

Then call `BoxPlot`: 
<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

val data: Seq[Seq[Double]] = // 
BoxPlot(data, boxRenderer = Some(tufteLikeBoxRenderer))
	.frame()
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/plot-catalog/tufte_box_plot.png" class="img-responsive"/>
</div>
</div>
 -->
## Pairs Plot
A pairs plot can be built by combining `ScatterPlot` and `Histogram` plots with `Facet`.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import scala.util.Random

val labels = Vector("a", "b", "c", "d")
val data = for (i <- 1 to 4) yield {
  (labels(i - 1), Seq.fill(10) { Random.nextDouble() * 10 })
}
val plots = for ((xlabel, xdata) <- data) yield {
  for ((ylabel, ydata) <- data) yield {
    val points = (xdata, ydata).zipped.map { (a, b) => Point(a, b) }
    if (ylabel == xlabel) {
      Histogram(xdata, bins = 4)
    } else {
      ScatterPlot(points)
    }
  }
}
Facets(plots)
  .standard()
  .title("Pairs Plot with Histograms")
  .topLabels(data.map { _._1 })
  .rightLabels(data.map { _._1 })
  .render()
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/plot-catalog/custom_pairs_plot.png" class="img-responsive"/>
</div>
</div>
