---
layout: docs
title: Drawing API
---
# Low-Level Drawing API

### Drawing Primitives

In EvilPlot, we represent everything that can be drawn to the screen as a `Drawable`.

A `Drawable` is simply a _description_ of the scene, and constructing one does not actually render anything. There are only a handful of drawing primitives that exist within EvilPlot, and they can be divided into three categories: drawing, positioning, and style.
The drawing primitives are the "leaves" of a scene. They represent concrete things like shapes and text. They are: 
<div class="container">
<div class="row">

<div class="col-md-3">
<img src="/cibotech/evilplot/img/docs/drawing-api/line.png" class="img-responsive"/>
<code>Line</code>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/path.png" class="img-responsive"/>
<code>Path</code>
</div>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/rect.png" class="img-responsive"/>
<code>Rect</code>
</div>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/borderrect.png" class="img-responsive"/>
<code>BorderRect</code>
</div>
</div>

</div>

<div class="row">

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/disc.png" class="img-responsive"/>
<code>Disc</code>
</div>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/wedge.png" class="img-responsive"/>
<code>Wedge</code>
</div>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/polygon.png" class="img-responsive"/>
<code>Polygon</code>
</div>
</div>

<div class="col-md-3">
<div class="center-block">
<img src="/cibotech/evilplot/img/docs/drawing-api/text.png" class="img-responsive"/>
<code>Text</code>
</div>
</div>

</div>
</div>

### Positioning Primitives

Positioning primitives alter where other `Drawable`s are placed. Notably, `Drawable` scenes do not expose a notion of absolute coordinates.

<div class="container">
<div class="row">
<div class="col-md-3">
<img src="/cibotech/evilplot/img/docs/drawing-api/translate.png" class="img-responsive"/>
<code>Translate</code>
</div>
<div class="col-md-3">
<img src="/cibotech/evilplot/img/docs/drawing-api/rotate.png" class="img-responsive"/>
<code>Rotate</code>
</div>
<div class="col-md-3">
<img src="/cibotech/evilplot/img/docs/drawing-api/scale.png" class="img-responsive"/>
<code>Scale</code>
</div>
<div class="col-md-3">
<img src="/cibotech/evilplot/img/docs/drawing-api/affine.png" class="img-responsive"/>
<code>Affine</code>
</div>
</div>
</div>

### Styling Primitives

Lastly, styling primitives allow us to modify the appearance of scenes.

+ `Style` colors the inside of a `Drawable`
+ `StrokeStyle` colors the outline of a `Drawable`
+ `StrokeWeight` changes the thickness of the outline of a `Drawable`
+ `LineDash` applies a [line style](/cibotech/evilplot/scaladoc/jvm/com/cibo/evilplot/geometry/LineStyle.html) to the outline of a `Drawable`

### Composition

Creating scenes is simply a matter of composing these primitives. For example, to place a blue rectangle next to a red circle, you might write:
<div class="row">
<div class="col-md-6" markdown="1">
```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.colors._
val rect = Style(Rect(400, 400), RGB(83, 87, 79))
Group(
  Seq(
    Style(
      Translate(Disc(200), x = rect.extent.width),
      RGB(78, 89, 94)
    ),
    rect
  )
)
```
</div>
<div class="col-md-6">
<img src="/cibotech/evilplot/img/docs/drawing-api/initialexample.png" class="img-responsive">
</div>
</div>

The `geometry` package also provides convenient syntax for dealing with positioning and styling primitives. So, instead of nesting constructors like we did up there, we can the following with the same result.

```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.colors._

val rect = Rect(400, 400) filled RGB(83, 87, 79)
Disc(200) transX rect.extent.width filled RGB(7) behind rect
```

The drawing API gives us the power to describe all of the scenes involved in the plots that EvilPlot can create; at no point do plot implementations reach below it and onto the rendering target. 

### Positional Combinators

EvilPlot gives a higher-level vocabulary to refer to common geometric manipulations. In fact, above we just wrote a positional combinator called `beside`. It turns out that the `geometry` package is full of similar combinators, so most of the time you'll never have to manually think about shifting objects by their widths like we did above.

<div class="row">
<div class="col-md-4">
<img src="/cibotech/evilplot/img/docs/drawing-api/beside.png" class="img-responsive"/>
</div>
<div class="col-md-4">
<img src="/cibotech/evilplot/img/docs/drawing-api/behind.png" class="img-responsive"/>
</div>
<div class="col-md-4">
<img src="/cibotech/evilplot/img/docs/drawing-api/above.png" class="img-responsive"/>
</div>
</div>

### Alignment

Additionally, you can align an entire sequence of `Drawable`s by calling one of the `Align` methods, which produce new sequence with all elements aligned. Combining alignment with reducing using a binary position operator is especially helpful:


```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.colors.HTMLNamedColors._

val aligned: Seq[Drawable] = Align.right(
	Rect(40, 60) filled blue,
	Disc(70) filled red,
	Polygon(Seq(Point(20, 60), Point(40, 20), Point(30, 30)) filled green)
)

aligned.reduce(_ below _)
```

The available alignment functions are:[^1].


<!-- ugh fix this alignment -->
<div class="container">
<div class="row">
<div class="col-md-3">
<div class="center-block">
<code>Align.bottom</code>
<img src="/cibotech/evilplot/img/docs/drawing-api/alignbottom.png" class="img-responsive"/>
</div>
</div>
<div class="col-md-3">
<code>Align.right</code>
<img src="/cibotech/evilplot/img/docs/drawing-api/alignright.png" class="img-responsive"/>
</div>
<div class="col-md-3">
<code>Align.middle</code>
<img src="/cibotech/evilplot/img/docs/drawing-api/alignmiddle.png" class="img-responsive"/>
</div>
<div class="col-md-3">
<code>Align.center</code>
<img src="/cibotech/evilplot/img/docs/drawing-api/aligncenter.png" class="img-responsive"/>
</div>
</div>
</div>

### An example

With EvilPlot's drawing combinators on our side, we're well equipped to recreate the box example from above that we made using only primitives--except it will be far easier to reason about. Recall, first we created our elements: two lines and two boxes, then centered them vertically and placed them on top of each other.


<div class="row">
<div class="col-md-8" markdown="1">
```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.colors.HTMLNamedColors.{blue, white}

// Some values from the outside:
val width = 10
val topWhisker = 50
val bottomWhisker = 30
val upperToMiddle = 40
val middleToLower = 35
val strokeWidth = 2


Align.center(
  Line(topWhisker, strokeWidth).rotated(90),
  BorderRect.filled(width, upperToMiddle),
  BorderRect.filled(width, middleToLower),
  Line(bottomWhisker, strokeWidth).rotated(90) 
).reduce(_ above _)
 .colored(blue)
 .filled(white)
```
</div>
<div class="col-md-4">
<img src="/cibotech/evilplot/img/docs/drawing-api/box2.png" class="img-responsive"/>
</div>
</div>

And in fact, if you were to look at the source code for EvilPlot's default box plot, it would be almost identical. 

### Drawing to the screen

Of course, at some point we want to draw our scenes to the screen. To do that, we call `draw()`

```scala
trait Drawable {
	// ...

	def draw(ctx: RenderContext): Unit
}
```

The `RenderContext` is the interface between our drawing API and platform-specific rendering targets. For more information on how to obtain a `RenderContext`, see the (docs page)[render-context.html].

[^1]: A `.reduce(_ above _)` or `.reduce(_ beside _)` was applied to each of these so the examples didn't stomp all over each other.

