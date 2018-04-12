---
layout: docs
title: Render Context 
---

# Getting a `RenderContext`

A `RenderContext` is the final target for all drawing operations in EvilPlot--it's where a constructed `Drawable` goes to ultimately be put on a screen.

## CanvasRenderContext

A `CanvasRenderContext` allows you to render to an HTML5 canvas element.

## Text metrics buffer
<!-- We should change the measureBuffer to evilplotMeasureBuffer and maybe just make one if it doesn't exist? -->
EvilPlot's canvas rendering backend requires a buffer for text measurements, which have to be made to construct `Drawable` objects if they contain `Text`. EvilPlot searches for a canvas element called `measureBuffer`, so you must have one in your page for it to work.


