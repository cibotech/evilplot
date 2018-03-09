package com.cibo.evilplot.geometry

import java.awt.geom.GeneralPath
import java.awt.{BasicStroke, Font, Graphics2D, RenderingHints}

import com.cibo.evilplot.colors.{Clear, Color, ColorUtils, HSLA}

import scala.collection.JavaConverters._
import scala.collection.mutable

final case class Graphics2DRenderContext(graphics: Graphics2D)
    extends RenderContext
    with Graphics2DSupport {
  import Graphics2DRenderContext._

  // Initialize based on whatever state the passed in graphics has.
  private[this] val initialState = GraphicsState(graphics.getTransform,
                                                 graphics.getPaint,
                                                 graphics.getColor,
                                                 graphics.getStroke)

  private val stateStack: mutable.ArrayStack[GraphicsState] =
    mutable.ArrayStack(initialState)

  private def enableAntialiasing(): Unit = {
    val renderingHints = Map(
      RenderingHints.KEY_ANTIALIASING -> RenderingHints.VALUE_ANTIALIAS_ON,
      RenderingHints.KEY_TEXT_ANTIALIASING -> RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    ).asJava

    graphics.setRenderingHints(renderingHints)
  }

  enableAntialiasing()
  graphics.setBackground(Clear.asJava)

  // Query graphics for state and store it.
  private def save(): Unit = {
    stateStack.push(
      GraphicsState(
        graphics.getTransform,
        graphics.getPaint,
        graphics.getColor,
        graphics.getStroke
      ))
  }

  private def restore(): Unit = {
    val current = stateStack.pop()
    graphics.setTransform(current.affineTransform)
    graphics.setPaint(current.fillColor)
    graphics.setColor(current.strokeColor)
    graphics.setStroke(current.strokeWeight)
  }

  def draw(line: Line): Unit = applyOp(this) {
    val stroke = line.strokeWidth.asStroke
    graphics.setStroke(stroke)
    val gpath = new GeneralPath()
    gpath.moveTo(0, line.strokeWidth / 2.0)
    gpath.lineTo(line.length, line.strokeWidth / 2.0)
    gpath.closePath()
    graphics.draw(gpath)
  }

  def draw(path: Path): Unit = applyOp(this) {
    val correction = path.strokeWidth / 2
    val stroke = path.strokeWidth.asStroke
    graphics.setStroke(stroke)
    val gpath = new GeneralPath()
    gpath.moveTo(path.points.head.x - correction,
                 path.points.head.y + correction)
    path.points.tail.foreach { point =>
      gpath.lineTo(point.x - correction, point.y + correction)
    }
    gpath.closePath()
    graphics.draw(gpath)
  }

  def draw(rect: Rect): Unit = applyOp(this) {
    graphics.fillRect(0, 0, rect.width.toInt, rect.height.toInt)
  }

  def draw(rect: BorderRect): Unit = applyOp(this) {
    graphics.drawRect(0, 0, rect.width.toInt, rect.height.toInt)
  }

  def draw(disc: Disc): Unit = applyOp(this) {
    val diameter = (2 * disc.radius).toInt
    graphics.fillArc(disc.x.toInt, disc.y.toInt, diameter, diameter, 0, 360)
  }

  def draw(wedge: Wedge): Unit = applyOp(this) {
    graphics.translate(wedge.radius, wedge.radius)
    graphics.fillArc(0,
                     0,
                     wedge.extent.width.toInt,
                     wedge.extent.height.toInt,
                     0,
                     360)
  }

  def draw(translate: Translate): Unit = applyOp(this) {
    graphics.translate(translate.x, translate.y)
    translate.r.draw(this)
  }

  def draw(affine: Affine): Unit = applyOp(this) {
    graphics.setTransform(affine.affine.asJava)
    affine.r.draw(this)
  }

  def draw(scale: Scale): Unit = applyOp(this) {
    graphics.scale(scale.x, scale.y)
    scale.r.draw(this)
  }

  def draw(rotate: Rotate): Unit = applyOp(this) {
    graphics.translate(-1 * rotate.minX, -1 * rotate.minY)
    graphics.rotate(math.toRadians(rotate.degrees))
    graphics.translate(rotate.r.extent.width / -2, rotate.r.extent.height / -2)
    rotate.r.draw(this)
  }

  def draw(style: Style): Unit = applyOp(this) {
    val javaColor = style.fill.asJava
    graphics.setPaint(javaColor)
    style.r.draw(this)
  }

  def draw(style: StrokeStyle): Unit = applyOp(this) {
    val javaColor = style.fill.asJava
    graphics.setColor(javaColor)
    style.r.draw(this)
  }

  def draw(weight: StrokeWeight): Unit = applyOp(this) {
    val stroke = weight.weight.asStroke
    graphics.setStroke(stroke)
    weight.r.draw(this)
  }

  def draw(text: Text): Unit = applyOp(this) {
    val baseExtent = TextMetrics.measure(text)
    val scalex = text.extent.width / baseExtent.width
    val scaley = text.extent.height / baseExtent.height
    graphics.scale(scalex, scaley)
    graphics.setFont(sansSerif.deriveFont(text.size.toFloat))
    // EvilPlot assumes all objects start at upper left,
    // but baselines for java.awt.Font do not refer to the top.
    graphics.drawString(text.msg, 0, baseExtent.height.toInt)
  }
}
object Graphics2DRenderContext {
  private[geometry] def applyOp(
      graphics2DRenderContext: Graphics2DRenderContext)(f: => Unit): Unit = {
    graphics2DRenderContext.save()
    f
    graphics2DRenderContext.restore()
  }

  private val sansSerif = Font.decode(Font.SANS_SERIF)
}

// To implement a RenderContext, we need to keep track of the target's
// current transform, fill color ("Paint"), stroke color ("Color"),
// and stroke weight ("Stroke")
private[geometry] final case class GraphicsState(
    affineTransform: java.awt.geom.AffineTransform,
    fillColor: java.awt.Paint,
    strokeColor: java.awt.Color,
    strokeWeight: java.awt.Stroke
)

private[geometry] trait Graphics2DSupport {
  implicit class ColorConverters(c: Color) {
    def asJava: java.awt.Color = c match {
      case hsla: HSLA =>
        val (r, g, b, a) = ColorUtils.hslaToRgba(hsla)
        new java.awt.Color(r.toFloat, g.toFloat, b.toFloat, a.toFloat)
      case Clear => new java.awt.Color(0.0f, 0.0f, 0.0f, 0.0f)
    }
  }

  implicit class TransformConverters(affine: AffineTransform) {
    def asJava: java.awt.geom.AffineTransform = {
      new java.awt.geom.AffineTransform(affine.scaleX,
                                        affine.shearY,
                                        affine.shearX,
                                        affine.scaleY,
                                        affine.shiftX,
                                        affine.shiftY)
    }
  }

  implicit class StrokeWeightConverters(strokeWeight: Double) {
    def asStroke: java.awt.Stroke = new BasicStroke(strokeWeight.toFloat)
  }
}
