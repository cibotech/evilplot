package com.cibo.evilplot.geometry

import java.awt.Stroke

// To implement a RenderContext, we need to keep track of the target's
// current transform, fill color ("Paint"), stroke color ("Color"),
// and stroke weight ("Stroke")
private[geometry] final case class GraphicsState(
                                           affineTransform: java.awt.geom.AffineTransform,
                                           fillColor: java.awt.Paint,
                                           strokeColor: java.awt.Color,
                                           strokeWeight: java.awt.Stroke
                                               )
