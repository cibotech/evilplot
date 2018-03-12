package com.cibo

import java.awt.image.BufferedImage

import com.cibo.evilplot.geometry._
package object evilplot {
  implicit class AwtDrawableOps(r: Drawable) {
    /** Return a BufferedImage containing the contents of this Drawable. */
    def asBufferedImage: BufferedImage = {
      val paddingHack = 20
      val bi = new BufferedImage(r.extent.width.toInt,
                                 r.extent.height.toInt,
                                 BufferedImage.TYPE_INT_ARGB)
      val gfx = bi.createGraphics()
      val padded = r.padAll(paddingHack / 2)

      fit(padded, r.extent).draw(Graphics2DRenderContext(gfx))
      gfx.dispose()
      bi
    }
  }
}
