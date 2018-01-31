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
    val correction = path.strokeWidth / 2
    canvas.beginPath()
    canvas.moveTo(path.points.head.x - correction, path.points.head.y + correction)
    canvas.lineWidth = path.strokeWidth
    path.points.tail.foreach { point =>
      canvas.lineTo(point.x - correction, point.y + correction)
    }
    canvas.stroke()
  }

  def draw(rect: Rect): Unit = canvas.fillRect(0, 0, rect.width, rect.height)

  def draw(rect: BorderRect): Unit = canvas.strokeRect(0, 0, rect.width, rect.height)

  def draw(disc: Disc): Unit = CanvasOp(canvas) {
    canvas.beginPath()
    canvas.arc(disc.x, disc.y, disc.radius, 0, 2 * Math.PI)
    canvas.closePath()
    canvas.fill()
  }

  def draw(wedge: Wedge): Unit = CanvasOp(canvas) {
    canvas.translate(wedge.radius, wedge.radius)
    canvas.beginPath()
    canvas.moveTo(0, 0)
    canvas.arc(0, 0, wedge.radius,
      -Math.PI * wedge.degrees / 360.0,
      Math.PI * wedge.degrees / 360.0
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
    canvas.rotate(math.toRadians(rotate.degrees))
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
    canvas.lineWidth = weight.weight
    weight.r.draw(this)
  }

  def draw(text: Text): Unit = TextMetrics.withStyle(text.size) { c =>
    c.fillText(text.msg, 0, 0)
  }(canvas)
}

