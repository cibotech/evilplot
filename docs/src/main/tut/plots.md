---
layout: docs
title: Plotting
position: 3
---

# Plots

In the last section, we made a simple scatter plot using a built-in plot method in EvilPlot. In this section, we're
going to explore some more built-in plots.

## `ScatterPlot`: The Whole Story

If we take a look at the method signature for `ScatterPlot.apply`, we see it takes more than just data:
```scala
object ScatterPlot {
  def apply(
    data: Seq[Point],
    pointRenderer: Option[PointRenderer] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = ???
}
```

Before, we passed the defaults for all parameters in `ScatterPlot` and let our theme determine how things looked.
But what if we want to color each point differently depending on the value of another variable? That's where the
`pointRenderer` argument comes in.

A `PointRenderer` tells your plot how to draw the data. When we don't pass one into `ScatterPlot`, we use
`PointRenderer.default()`, which plots each item in the `data` sequence as a disc filled with the color in
`theme.colors.fill`. But, there are a bunch more [`PointRenderer`s to choose from](/cibotech/evilplot/scaladoc/jvm/com/cibo/evilplot/plot/renderers/PointRenderer$.html).

<div class="row">
<div class="col-md-6" markdown="1">

```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.renderers.PointRenderer
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import scala.util.Random

val qualities = Seq("good", "bad")

final case class MyFancyData(x: Double, y: Double, quality: String)
val data: Seq[MyFancyData] = Seq.fill(100) {
  MyFancyData(Random.nextDouble(), Random.nextDouble(), qualities(Random.nextInt(2)))
}
val points = data.map(d => Point(d.x, d.y))

ScatterPlot(
  points,
  pointRenderer = Some(PointRenderer.colorByCategory(data.map(_.quality)))
).xAxis()
 .yAxis()
 .frame()
 .rightLegend()
 .render()
```
</div>
<div class="col-md-6">
  <img src="/cibotech/evilplot/img/docs/plots/pointrenderer.png" class="img-responsive"/>
</div>
</div>

The `colorByCategory` point renderer handled our categories for us. You'll notice we included a call to `.rightLegend()`
as well. The renderer is able to add our categories and their associated aesthetics to a context for a legend
automatically, so we get a legend that makes sense.

## Overlaying

EvilPlot takes an unusual approach to overlaying, in the spirit of building more complex visualizations out of simpler
ones. When you want to make a multilayered plot, just make a plot for each layer. We then give you a combinator,
`Overlay`, to compose all the plots into a meaningful composite.

Let's look at two equally meaningful plots. The first is a
[Ramachandran plot](https://en.wikipedia.org/wiki/Ramachandran_plot), which is a plot of the phi and psi dihredral
angles in a peptide bond. This data came from a molecular dynamics simulation of alanine dipeptide, so we have a
computed "phi" and "psi" from the simulation for each iteration. We want to look at how these two angles were
distributed, so let's make a contour plot out of the density.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors.HTMLNamedColors._
ContourPlot(
  AlanineData.data,
  surfaceRenderer = Some(SurfaceRenderer.contours(Some(dodgerBlue)))
)
  .xLabel("phi")
  .yLabel("psi")
  // For reference, fix the bounds over the allowed phi/psi ranges.
  .xbounds(-180, 180)
  .ybounds(-180, 180)
  .xAxis()
  .yAxis()
  .frame()
```
</div>
<div class="col-md-6">
  <img src="/cibotech/evilplot/img/docs/plots/contour.png" class="img-responsive"/>
</div>
</div>

We might wonder what configuration the peptide was in at the beginning of the simulation. Let's put a point on
the chart that shows the initial configuration.
<div class="row">
<div class="col-md-6" markdown="1">
```scala
val initial = ScatterPlot(
  AlanineData.data.head,
  pointRenderer = Some(PointRenderer.default(color = Some(crimson)))
)

val contours = ContourPlot(
  AlanineData.data,
  surfaceRenderer = Some(SurfaceRenderer.contours(Some(dodgerBlue)))
)

Overlay(contours, initial)
  .xLabel("phi")
  .yLabel("psi")
  .xbounds(-180, 180)
  .ybounds(-180, 180)
  .xAxis()
  .yAxis()
  .frame()
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/plots/withinitialpoint.png" class="img-responsive"/>
</div>
</div>

With `Overlay`, we don't have to worry that the bounds of our plots might not align--EvilPlot will take care of all of
the necessary transformations for you.

## Adding side plots.

The contour plot gives us a good glimpse at the bivariate distribution. But, let's say we were secondarily interested in
looking at the univariate distribution of each parameter. Of course, we _could_ just make histograms.

```scala
val phiHistogram = Histogram(AlanineData.data.map(_.x))
val psiHistogram = Histogram(AlanineData.data.map(_.y))
```

But we don't have to stop at plotting these separately. EvilPlot will let us place plots in the margins of another plot
using a whole family of border plot combinators. If we take what we'd been working on from before:

<div class="row">
<div class="col-md-6" markdown="1">
```scala
Overlay(contours, initial)
  .topPlot(phiHistogram)
  .rightPlot(psiHistogram)
  .xLabel("phi")
  .yLabel("psi")
  .frame()
```
</div>
<div class="col-md-6">
  <img src="/cibotech/evilplot/img/docs/plots/sideplots.png" class="img-responsive"/>
</div>
</div>

## Faceting

Imagine that after you made the visualization above, it turned out that you want to run similar simulations at different
temperatures and additionally evaluate a different set of simulation parameters. This is a perfect candidate for
faceting.

<!-- TODO: Fix these axes. These axes are super ugly and -180-180 is standard for Rama plots -->

<div class="row">
<div class="col-md-6" markdown="1">
```scala
Facets(
  AlanineData.allDihedrals.map(
    _.map(ps =>
      ContourPlot(ps,
       surfaceRenderer = Some(SurfaceRenderer.contours(Some(dodgerBlue))))
        .overlay(ScatterPlot(ps.head,
           pointRenderer = Some(PointRenderer.default(Some(crimson)))))
        .topPlot(Histogram(ps.map(_.x)))
        .rightPlot(Histogram(ps.map(_.y)))
        .frame()
    )
  )
)
  .topLabels(AlanineData.temps.map(k => s"$k K"))
  .rightLabels(Seq("params1", "params2"))
  .xbounds(-180, 180)
  .ybounds(-180, 180)
  .xAxis(tickCount = Some(6))
  .yAxis(tickCount = Some(6))
  .xLabel("phi")
  .yLabel("psi")
  .render()
```
</div>
<div class="col-md-6">
  <img src="/cibotech/evilplot/img/docs/plots/facetedcontours.png" class="img-responsive"/>
</div>
</div>

Hopefully, this example convinces you that it's easy to start with a simple visual and compose more and more complexity
on top of it using plot combinators. We first saw that the base plot can be customized using `PlotRenderers`. After that
we saw how simple `Plot => Plot` combinators can add additional features piece-by-piece. Next, we saw that
"multilayer plots" are expressed in EvilPlot simply by applying `Overlay`, a function from `Seq[Plot] => Plot`.
Finally, we saw how we can build a multivariate, faceted display out of other plots easily, regardless of how
complicated the individual plots are.

But, we're just getting started. Check out the [Plot Catalog](plot-catalog.html) for awesome examples of what you can do
with EvilPlot. Or, read about the [drawing API](drawing-api.html) for some background before venturing into writing your
own renderers, which is an easy way to make EvilPlot even more customizable.
