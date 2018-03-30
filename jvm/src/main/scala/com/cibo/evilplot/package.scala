package com.cibo

import java.awt.image.BufferedImage

import com.cibo.evilplot.geometry._
import javax.imageio.ImageIO
package object evilplot {
  implicit class AwtDrawableOps(r: Drawable) {
    /** Return a BufferedImage containing the contents of this Drawable. */
    def asBufferedImage: BufferedImage = {
      val scale = 2
      val paddingHack = 20
      val bi = new BufferedImage(r.extent.width.toInt * scale,
        r.extent.height.toInt * scale,
        BufferedImage.TYPE_INT_ARGB)
      val gfx = bi.createGraphics()
      gfx.scale(2.0, 2.0)
      val padded = r.padAll(paddingHack / 2)
      fit(padded, r.extent).draw(Graphics2DRenderContext(gfx))
      gfx.dispose()
      bi
    }

    /** Write a Drawable to a file as a PNG. */
    def write(file: java.io.File): Unit = {
      ImageIO.write(asBufferedImage, "png", file)
    }
  }
}
