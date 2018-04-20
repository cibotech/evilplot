/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo

import java.awt.image.BufferedImage

import com.cibo.evilplot.geometry._
import javax.imageio.ImageIO
package object evilplot {
  implicit class AwtDrawableOps(r: Drawable) {

    /** Return a BufferedImage containing the contents of this Drawable. */
    def asBufferedImage: BufferedImage = {
      val scale = 4.0
      val paddingHack = 20
      val bi = new BufferedImage(
        (r.extent.width * scale.toInt).toInt,
        (r.extent.height * scale).toInt,
        BufferedImage.TYPE_INT_ARGB)
      val gfx = bi.createGraphics()
      gfx.scale(scale, scale)
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
