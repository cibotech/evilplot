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

package com.cibo.evilplot.geometry

import org.scalajs.dom.raw.CanvasRenderingContext2D

final case class CanvasRenderContext(canvas: CanvasRenderingContext2D) extends RenderContext {
  def draw(line: Line): Unit = CanvasOp(canvas) {
    canvas.lineWidth = line.strokeWidth
    canvas.beginPath()
    canvas.moveTo(0, line.strokeWidth / 2.0)
    canvas.lineTo(line.length, line.strokeWidth / 2.0)
    canvas.closePath()
    canvas.stroke()
  }

  def draw(path: Path): Unit = CanvasOp(canvas) {
    canvas.lineJoin = "round"
    canvas.beginPath()
    canvas.moveTo(path.points.head.x, path.points.head.y)
    canvas.lineWidth = path.strokeWidth
    path.points.tail.foreach { point =>
      canvas.lineTo(point.x, point.y)
    }
    canvas.stroke()
  }

  def draw(polygon: Polygon): Unit = CanvasOp(canvas) {
    canvas.beginPath()
    canvas.moveTo(polygon.boundary.head.x, polygon.boundary.head.y)
    polygon.boundary.tail.foreach { point =>
      canvas.lineTo(point.x, point.y)
    }
    canvas.fill()
  }

  def draw(rect: Rect): Unit = canvas.fillRect(0, 0, rect.width, rect.height)

  def draw(rect: BorderRect): Unit = canvas.strokeRect(0, 0, rect.width, rect.height)

  def draw(disc: Disc): Unit = CanvasOp(canvas) {
    canvas.beginPath()
    canvas.arc(disc.radius, disc.radius, disc.radius, 0, 2 * Math.PI)
    canvas.closePath()
    canvas.fill()
  }

  def draw(wedge: Wedge): Unit = CanvasOp(canvas) {
    canvas.translate(wedge.radius, wedge.radius)
    canvas.beginPath()
    canvas.moveTo(0, 0)
    canvas.arc(0, 0, wedge.radius, 0, 2 * Math.PI * wedge.degrees / 360.0)
    canvas.closePath()
    canvas.fill()
  }

  def draw(translate: Translate): Unit = CanvasOp(canvas) {
    canvas.translate(translate.x, translate.y)
    translate.r.draw(this)
  }

  def draw(affine: Affine): Unit = CanvasOp(canvas) {
    canvas.transform(
      affine.affine.scaleX,
      affine.affine.shearX,
      affine.affine.shearY,
      affine.affine.scaleY,
      affine.affine.shiftX,
      affine.affine.shiftY
    )
    affine.r.draw(this)
  }

  def draw(scale: Scale): Unit = CanvasOp(canvas) {
    canvas.scale(scale.x, scale.y)
    scale.r.draw(this)
  }

  def draw(rotate: Rotate): Unit = CanvasOp(canvas) {
    canvas.translate(-1 * rotate.minX, -1 * rotate.minY)
    canvas.rotate(math.toRadians(rotate.degrees))
    canvas.translate(rotate.r.extent.width / -2, rotate.r.extent.height / -2)
    rotate.r.draw(this)
  }

  def draw(style: Style): Unit = CanvasOp(canvas) {
    canvas.fillStyle = style.fill.repr
    style.r.draw(this)
  }

  def draw(style: StrokeStyle): Unit = CanvasOp(canvas) {
    canvas.strokeStyle = style.fill.repr
    style.r.draw(this)
  }

  def draw(weight: StrokeWeight): Unit = CanvasOp(canvas) {
    canvas.lineWidth = weight.weight
    weight.r.draw(this)
  }

  def draw(lineDash: LineDash): Unit = CanvasOp(canvas) {
    import scalajs.js.JSConverters._
    canvas.setLineDash(lineDash.style.dashPattern.toJSArray)
    canvas.lineDashOffset = lineDash.style.offset
    lineDash.r.draw(this)
  }

  def draw(text: Text): Unit = {

    // Adjust the size of the font to fill the requested extent.
    // text.size assumes that the text will fill text.extent, but
    // in reality, it will fill baseExtent.
    // So we need to scale the size to fill text.extent.
    val baseExtent = TextMetrics.measure(text)
    val scalex = text.extent.width / baseExtent.width
    val scaley = text.extent.height / baseExtent.height

    CanvasOp(canvas) {
      canvas.scale(scalex, scaley)
      TextMetrics.withStyle(text.size, text.fontFace) { c =>
        c.fillText(text.msg, 0, 0)
      }(canvas)
    }
  }
}
