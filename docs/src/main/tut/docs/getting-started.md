---
layout: docs
title: Getting Started
---
# Getting Started

To get going with EvilPlot, you'll need to add it to your build.
```scala
resolvers += Seq(Resolver.bintrayRepo("cibotech", "public"))
libraryDependencies += "com.cibo" %%% "evilplot" % "0.2.0"
```

## Our first plot

EvilPlot is all about building larger graphical capabilities out of smaller ones. What this means for you is that making
simple plots is easy, and we don't have to spend a lot of time going over all the features just to get started. So let's make
our first plot, a simple scatter plot with sequential x-values and random y-values.

```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point

val data = Seq.tabulate(100)(i => Point(i.toDouble, scala.util.Random.nextDouble()))
ScatterPlot(data).render()
```

To break it down:
+ EvilPlot uses an implicit themeing system, which lets you control the appearance of plots wholesale. In this case, we're just using the built-in one, so we import it.
+ `ScatterPlot` returns a `Plot` object, which is a description of how data should be plotted (plot these points as little circles on the screen), with what components (like axes, a background etc.). In this case, we've used _no_ components. All we get is points on the screen!
+ Finally, `render()` on `Plot` returns a `Drawable` object. Think of a `Drawable` as a fully specified description of a scene. `render` itself does not perform any side-effects, it simply constructs a scene given a plot and a size.

This plot is not very interesting, of course. We should probably add some axes, so we know the range of the data, and some labels, so our audience knows what we're talking about. That's easy as well:

```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point

val data = Seq.tabulate(100)(i => Point(i.toDouble, scala.util.Random.nextDouble()))
ScatterPlot(data)
  .xAxis()
  .yAxis()
  .xGrid()
  .yGrid()
  .background()
  .xLabel("x")
  .yLabel("y")
  .render()
```

Adding these things is simply a matter of calling an additional method on your plot that specifies _exactly_ what type of component you want to add. You might think that calling `.xAxis().yAxis().xGrid().yGrid().background()` for all of your plots is a bit verbose. The `.standard()` method is a shortcut for that very common set of components. So, our plot could be:

```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point

val data = Seq.tabulate(100)(i => Point(i.toDouble, scala.util.Random.nextDouble()))
ScatterPlot(data)
  .standard()
  .xLabel("x")
  .yLabel("y")
  .render()
```

## Plot Construction

Later on we'll see that EvilPlot actually does not have different types for different kinds of plot -- everything is "just a `Plot`." `ScatterPlot` is just a convenience function that wraps the constructor for `Plot`. These convenience functions are likely what you'll start with most of the time that you use EvilPlot, and there are a bunch of them in the `plot` package:

+ ScatterPlot
+ LinePlot
+ ContourPlot
+ BoxPlot
+ BarChart
+ Histogram
+ Heatmap
+ Facets
+ Overlay

Through this tutorial, we'll see examples of using all of these, but the pattern is mostly the same as what we've seen above: call it with your data, add your components, convert to a scene.

The rest of this tutorial is split up as follows:
+ A tour of the drawing API.
+ How to use plot element renderers, which allow you to fully customize your plots.
+ Plot themes
+ The `Plot` data type itself, and implementing completely custom plots.

