---
layout: docs
title: Drawing API
---
# Low-Level Drawing API

## Drawing Primitives

In EvilPlot, we represent everything that can be drawn to the screen as a `Drawable`.

A `Drawable` is simply a _description_ of the scene, and constructing one does not actually render anything. There are only a handful of drawing primitives that exist within EvilPlot, and they can be divided into three categories: drawing, positioning, and style.

_Drawing_:
* EmptyDrawable
* Line
* Path
* Rect
* BorderRect
* Disc
* Wedge
* Text
_Positioning_:
* Translate
* Affine
* Scale
* Rotate
* Group
* Resize
_Styling_:
* Style
* StrokeStyle
* StrokeWeight


Creating scenes is simply a matter of using these primitives. For example, to place a blue rectangle next to a red circle, you might write:
```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.color.HTMLNamedColors
val rect = Style(Rect(40, 40), HTMLNamedColors.blue)
Group(Style(Translate(Disc(20), x = rect.extent.width), HTMLNamedColors.red), rect)
```

The `geometry` package also provides convenient syntax for dealing with positioning and styling primitives. So, instead of nesting constructors like we did up there, we can write:

```scala
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.color.HTMLNamedColors

val rect = Rect(40, 40) filled HTMLNamedColors.blue
Disc(20) transX rect.extent.width filled HTMLNamedColors.red behind rect
```

## Positional Combinators

EvilPlot is all about giving a higher-level vocabulary to refer to common geometric manipulations. In fact, above we just wrote a positional combinator called `beside`. It turns out that the `geometry` package is full of similar combinators, so most of the time you'll never have to manually think about shifting objects by their widths like we did above.







