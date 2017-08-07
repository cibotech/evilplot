/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.layout

import com.cibo.evilplot.geometry.Drawable

/** Layouts take DrawableLaters, objects which take an extent and return a Drawable.
 * The Layouts determine the extents of the rendered drawables and place them appropriately.
 */

trait Layout extends Drawable {
}

