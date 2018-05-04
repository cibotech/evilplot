---
layout: docs
title: Colors
position: 6
---

# Colors

Colors in EvilPlot are described by their [HSL](https://en.wikipedia.org/wiki/HSL_and_HSV) representation.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
val transparentColor = HSLA(210, 100, 56, 0.8)

val color = HSL(210, 100, 56) // fully opaque
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/basic_hsl.png" class="img-responsive"/>
</div>
</div>

You can also use the RGB representation to describe a color. It will be converted to HSL automatically.
<div class="row">
<div class="col-md-6" markdown="1">
```scala
val transparentColor = RGBA(230, 126, 34, 0.8)

val color = RGB(230, 126, 34)

val hexColor = HEX("E67E22")
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/basic_rgb.png" class="img-responsive"/>
</div>
</div>

### Predefined colors

All of the HTML named colors are available to use:
<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

val htmlRed = HTMLNamedColors.red
val htmlDodgerBlue = HTMLNamedColors.dodgerBlue
val htmlTomato = HTMLNamedColors.tomato
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/basic_html.png" class="img-responsive"/>
</div>
</div>

### Modifying colors

You can create colors by modifying the HSLA properties of another color.
<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

val htmlRed = HTMLNamedColors.red
val transparentRed = htmlRed.copy(opacity = 0.5)
val difficultWhite = htmlRed.copy(lightness = 100)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/basic_mod.png" class="img-responsive"/>
</div>
</div>

EvilPlot has several helpful methods for modifying colors:
<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

val htmlRed = HTMLNamedColors.red

val lighterRed = htmlRed.lighten(20) // Increase lightness by 20
val darkerRed = htmlRed.darken(20)

// Two new colors evenly spaced around the HSL color wheel from the first
val (triad1, triad2) = htmlRed.triadic

// Two new colors adjacent to the first color on the color wheel
// (+/- 45 degrees away in this example)
val (analogue1, analogue2) = htmlRed.analogous(45)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/fancy_mod.png" class="img-responsive"/>
</div>
</div>

### Color Sequences and Gradients

A variety of streams and sequences of colors can be generated.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

val colors = Color.stream.take(40)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/stream.png" class="img-responsive"/>
</div>
</div>

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

// A gradient across all hues
val colors = Color.getGradientSeq(10)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/gradient_seq.png" class="img-responsive"/>
</div>
</div>

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

// A gradient between specific hues
val colors = Color.getGradientSeq(10, startHue = 0, endHue = 120)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/gradient_narrow_seq.png" class="img-responsive"/>
</div>
</div>

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

val colors = Color.getDefaultPaletteSeq(8)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/default_seq.png" class="img-responsive"/>
</div>
</div>

Data can be colored using a continuous gradient.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.renderers.PointRenderer

val gradient = ContinuousColoring.gradient(HTMLNamedColors.blue, HTMLNamedColors.orange)

val points = Seq.tabulate(100) { i =>
  Point(i.toDouble, (math.sin(i.toDouble / 10) * 50) + 50)
}
val renderer = PointRenderer.depthColor(points.map(_.y), Some(gradient))
ScatterPlot(points, pointRenderer = Some(renderer))
  .frame().xAxis().yAxis().xGrid().yGrid()
  .xbounds(0, 100).ybounds(0, 100)
  .render(Extent(500, 300))
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/continuous_gradient.png" class="img-responsive"/>
</div>
</div>

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._

// Multiple stop points can be given for continuous gradients
val gradient = ContinuousColoring.gradient(Seq(
  HTMLNamedColors.green,
  HTMLNamedColors.red,
  HTMLNamedColors.blue,
  HTMLNamedColors.red
), Some(0d), Some(40d), GradientMode.Linear)

val gradientFunc = gradient(Seq(0, 40))
val colors = Seq.tabulate(40) { gradientFunc(_) }
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/continuous_multigradient.png" class="img-responsive"/>
</div>
</div>

Categorical data can be colored using a categorical gradient.

<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.colors._
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.renderers.PointRenderer

val gradient: Coloring[Double] = CategoricalColoring.gradient(
  HTMLNamedColors.blue,
  HTMLNamedColors.orange
)

val points = Seq.tabulate(100) { i => Point(i.toDouble, i.toDouble) }
val renderer = PointRenderer.colorByCategory(points.map(p => p.x % 3), Some(gradient))
ScatterPlot(points, pointRenderer = Some(renderer))
  .frame().xAxis().yAxis().xGrid().yGrid()
  .xbounds(0, 100).ybounds(0, 100)
  .rightLegend()
  .render(Extent(500, 300))
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/colors/categorical_gradient.png" class="img-responsive"/>
</div>
</div>
