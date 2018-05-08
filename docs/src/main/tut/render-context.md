---
layout: docs
title: Render Contexts
position: 7
---

# RenderContext

A `RenderContext` is the final target for all drawing operations in EvilPlot--it's where a constructed `Drawable` goes
to ultimately be put on a screen. EvilPlot provides a `RenderContext` for each supported platform.

## CanvasRenderContext

`CanvasRenderContext` is for rendering to an HTML5 Canvas element and is available from within ScalaJS. To use it, you
must obtain a `CanvasRenderingContext2D` from a canvas element in your page. Here's the example plot from the Getting
Started page using `CanvasRenderContext`.

```scala
import com.cibo.evilplot.geometry.{CanvasRenderContext, Extent}
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point
import org.scalajs.dom

val canvas = dom.document.createElement("canvas").asInstanceOf[dom.html.Canvas]
canvas.width = 400
canvas.height = 400
dom.document.body.appendChild(canvas)
val context = CanvasRenderContext(canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D])

val data = Seq.tabulate(100) { i =>
  Point(i.toDouble, scala.util.Random.nextDouble())
}
ScatterPlot(data).render(Extent(400, 400)).draw(context)
```

### Canvas only: The text metrics buffer
EvilPlot's canvas rendering backend requires a buffer for text measurements, which have to be made to construct
`Drawable` objects if they contain `Text`. EvilPlot searches for a canvas element called `measureBuffer`, so you must
have one in your page for it to work. There is no such requirement on the JVM.

## Graphics2DRenderContext

EvilPlot also supports rendering to a `Graphics2D` in Java's AWT. You can obtain a `Graphics2DRenderContext` by passing
a `Graphics2D` to it. When you use the JVM version of EvilPlot, you can use the `asBufferedImage` method, which will
handle the creation of a `RenderContext` and the rendering process:

```scala
import com.cibo.evilplot._
import com.cibo.evilplot.geometry._

Rect(40, 40).asBufferedImage
```

