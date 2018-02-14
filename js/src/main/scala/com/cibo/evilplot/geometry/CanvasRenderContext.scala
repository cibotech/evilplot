package com.cibo.evilplot.geometry

import org.scalajs.dom.raw.CanvasRenderingContext2D

final case class CanvasRenderContext(canvas: CanvasRenderingContext2D) extends RenderContext {
  def draw(line: Line): Unit = CanvasOp(canvas) {
    canvas.lineWidth = line.w
    canvas.beginPath()
    canvas.moveTo(0, line.w / 2.0)
    canvas.lineTo(line.l, line.w / 2.0)
    canvas.closePath()
    canvas.stroke()
  }

  def draw(path: Path): Unit = CanvasOp(canvas) {
    val correction = path.w / 2
    canvas.beginPath()
    canvas.moveTo(path.ps.head.x - correction, path.ps.head.y + correction)
    canvas.lineWidth = path.w
    path.ps.tail.foreach { point =>
      canvas.lineTo(point.x - correction, point.y + correction)
    }
    canvas.stroke()
  }

  def draw(rect: Rect): Unit = canvas.fillRect(0, 0, rect.w, rect.h)

  def draw(rect: BorderRect): Unit = canvas.strokeRect(0, 0, rect.w, rect.h)

  def draw(disc: Disc): Unit = CanvasOp(canvas) {
    canvas.beginPath()
    canvas.arc(disc.x, disc.y, disc.r, 0, 2 * Math.PI)
    canvas.closePath()
    canvas.fill()
  }

  def draw(wedge: Wedge): Unit = CanvasOp(canvas) {
    canvas.translate(wedge.r, wedge.r)
    canvas.beginPath()
    canvas.moveTo(0, 0)
    canvas.arc(0, 0, wedge.r,
      -Math.PI * wedge.d / 360.0,
      Math.PI * wedge.d / 360.0
    )
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
    canvas.rotate(math.toRadians(rotate.d))
    canvas.translate(rotate.r.extent.width / -2, rotate.r.extent.height / -2)
    rotate.r.draw(this)
  }

  def draw(rotate: UnsafeRotate): Unit = CanvasOp(canvas) {
    canvas.translate(rotate.extent.width / 2, rotate.extent.height / 2)
    canvas.rotate(math.toRadians(rotate.degrees))
    canvas.translate(rotate.extent.width / -2, rotate.extent.height / -2)
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
    canvas.lineWidth = weight.w
    weight.r.draw(this)
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
      TextMetrics.withStyle(text.size) { c =>
        c.fillText(text.s, 0, 0)
      }(canvas)
    }
  }
}

