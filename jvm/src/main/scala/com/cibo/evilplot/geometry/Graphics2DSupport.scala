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

import java.awt.MultipleGradientPaint.CycleMethod
import java.awt.geom.GeneralPath
import java.awt.{Color => _, _}

import com.cibo.evilplot.colors._

import scala.collection.JavaConverters._
import scala.collection.mutable

/** A RenderContext for java.awt.Graphics2D
  * @param graphics the Graphics2D instance to render to.
  */
final case class Graphics2DRenderContext(graphics: Graphics2D)
    extends RenderContext
    with Graphics2DSupport {

  import Graphics2DRenderContext._

  private[geometry] val initialState = GraphicsState(
    graphics.getTransform,
    java.awt.Color.BLACK,
    java.awt.Color.BLACK,
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
  // Graphics2D does not distinguish between "fill" and "stroke" colors,
  // as both canvas and EvilPlot do, so we keep these locally as vars and
  // only push the change to the Graphics2D instance when we are performing
  // a fill/stroke operation.
  private[geometry] var _fillColor: java.awt.Paint = initialState.fillColor // scalastyle: ignore

  private[geometry] def fillColor_=(c: java.awt.Paint): Unit = {
    _fillColor = c
  }
  private[geometry] def fillColor: java.awt.Paint = _fillColor
  private[geometry] var strokeColor: java.awt.Paint = initialState.strokeColor // scalastyle:ignore

  enableAntialiasing()

  // Query graphics for state and store it.
  private def save(): Unit = {
    stateStack.push(
      GraphicsState(
        graphics.getTransform,
        fillColor,
        strokeColor,
        graphics.getStroke
      )
    )
  }

  private def restore(): Unit = {
    val restored = stateStack.pop()
    graphics.setTransform(restored.affineTransform)
    fillColor = restored.fillColor
    strokeColor = restored.strokeColor
    graphics.setStroke(restored.strokeWeight)
    // Clear out the graphics color.
    graphics.setPaint(initialState.fillColor)
  }

  def draw(line: Line): Unit = applyWithStrokeColor(this) {
    val stroke = graphics.getStroke
      .asInstanceOf[BasicStroke]
      .update(strokeWeight = Some(line.strokeWidth))
    graphics.setStroke(stroke)
    val gpath = new GeneralPath()
    gpath.moveTo(0, line.strokeWidth / 2.0)
    gpath.lineTo(line.length, line.strokeWidth / 2.0)
    gpath.closePath()
    graphics.draw(gpath)
  }

  def draw(polygon: com.cibo.evilplot.geometry.Polygon): Unit = applyWithFillColor(this) {
    val gpath = new GeneralPath()
    gpath.moveTo(polygon.boundary.head.x, polygon.boundary.head.y)
    polygon.boundary.tail.foreach { point =>
      gpath.lineTo(point.x, point.y)
    }
    gpath.closePath()
    graphics.fill(gpath)
  }

  def draw(path: Path): Unit = applyWithStrokeColor(this) {
    val stroke = graphics.getStroke
      .asInstanceOf[BasicStroke]
      .update(strokeWeight = Some(path.strokeWidth))

    graphics.setStroke(stroke)
    val gpath = new GeneralPath()
    gpath.moveTo(path.points.head.x, path.points.head.y)
    path.points.tail.foreach { point =>
      gpath.lineTo(point.x, point.y)
    }
    graphics.draw(gpath)
  }

  def draw(rect: Rect): Unit = applyWithFillColor(this) {
    graphics.fill(new java.awt.Rectangle(0, 0, rect.width.toInt, rect.height.toInt))
  }

  def draw(rect: BorderRect): Unit = applyWithStrokeColor(this) {
    graphics.draw(new java.awt.Rectangle(0, 0, rect.width.toInt, rect.height.toInt))
  }

  def draw(disc: Disc): Unit = applyWithFillColor(this) {
    // Note that unlike canvas, fillArc's x and y parameters refer to the top left corner.
    graphics.fillArc(
      0,
      0,
      disc.extent.width.toInt,
      disc.extent.height.toInt,
      0,
      360
    )
  }

  def draw(wedge: Wedge): Unit = applyWithFillColor(this) {
    val wedgeDiam = 2 * wedge.radius.toInt
    graphics.fillArc(
      0,
      0,
      wedgeDiam,
      wedgeDiam,
      0,
      wedge.degrees.toInt
    )
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
    fillColor = style.fill.asJava
    style.r.draw(this)
  }

  def draw(style: StrokeStyle): Unit = applyOp(this) {
    strokeColor = style.fill.asJava
    style.r.draw(this)
  }

  def draw(weight: StrokeWeight): Unit = applyOp(this) {
    val stroke = graphics.getStroke
      .asInstanceOf[BasicStroke]
      .update(strokeWeight = Some(weight.weight))

    graphics.setStroke(stroke)
    weight.r.draw(this)
  }

  def draw(lineDash: LineDash): Unit = applyOp(this) {
    val stroke = graphics.getStroke
      .asInstanceOf[BasicStroke]
      .update(lineStyle = Some(lineDash.style))

    graphics.setStroke(stroke)
    lineDash.r.draw(this)
  }

  def draw(text: Text): Unit = applyWithStrokeColor(this) {
    val baseExtent = TextMetrics.measure(text)
    val scalex = text.extent.width / baseExtent.width
    val scaley = text.extent.height / baseExtent.height
    graphics.scale(scalex, scaley)
    graphics.setFont(Font.decode(text.fontFace).deriveFont(text.size.toFloat))
    // EvilPlot assumes all objects start at upper left,
    // but baselines for java.awt.Font do not refer to the top.
    graphics.drawString(text.msg, 0, baseExtent.height.toInt)
  }

  def draw(gradient: GradientFill): Unit = {
    gradient.fill match {
      case lg: LinearGradient =>
        val gradientFill = new LinearGradientPaint(
          lg.x0.toFloat,
          lg.y0.toFloat,
          lg.x1.toFloat,
          lg.y1.toFloat,
          lg.stops.map(_.offset.toFloat).toArray,
          lg.stops.map(_.color.asJava).toArray
        )

        fillColor = gradientFill
        gradient.r.draw(this)

      case rg: RadialGradient =>
        val gradientFill = new RadialGradientPaint(
          rg.x0.toFloat,
          rg.y0.toFloat,
          rg.r0.toFloat,
          rg.x1.toFloat,
          rg.y1.toFloat,
          rg.stops.map(_.offset.toFloat).toArray,
          rg.stops.map(_.color.asJava).toArray,
          CycleMethod.NO_CYCLE
        )

        fillColor = gradientFill
        gradient.r.draw(this)
    }
  }

  // to implement, reference: InteractionMask.scala and InteractiveCanvasContext.scala
  override def draw(interaction: Interaction): Unit = interaction.r.draw(this)
}

object Graphics2DRenderContext {
  private[geometry] def applyOp(graphics2DRenderContext: Graphics2DRenderContext)(
    f: => Unit): Unit = {
    graphics2DRenderContext.save()
    f
    graphics2DRenderContext.restore()
  }

  private[geometry] def applyWithStrokeColor(graphics2DRenderContext: Graphics2DRenderContext)(
    f: => Unit
  ): Unit = {
    applyOp(graphics2DRenderContext) {
      graphics2DRenderContext.graphics.setPaint(graphics2DRenderContext.strokeColor)
      f
    }
  }

  private[geometry] def applyWithFillColor(graphics2DRenderContext: Graphics2DRenderContext)(
    f: => Unit
  ): Unit = {
    applyOp(graphics2DRenderContext) {
      graphics2DRenderContext.graphics.setPaint(graphics2DRenderContext.fillColor)
      f
    }
  }

  private val sansSerif = Font.decode(Font.SANS_SERIF)
}

private[geometry] final case class GraphicsState(
  affineTransform: java.awt.geom.AffineTransform,
  fillColor: java.awt.Paint,
  strokeColor: java.awt.Paint,
  strokeWeight: java.awt.Stroke
)

private[geometry] trait Graphics2DSupport {

  implicit class ColorConverters(c: Color) {
    def asJava: java.awt.Color = c match {
      case hsla: HSLA =>
        val (r, g, b, a) = ColorUtils.hslaToRgba(hsla)
        new java.awt.Color(r.toFloat, g.toFloat, b.toFloat, a.toFloat)
      case Clear => new java.awt.Color(0, 0, 0, 0)
      case ClearWhite => new java.awt.Color(255, 255, 255, 0)
    }
  }

  implicit class TransformConverters(affine: AffineTransform) {
    def asJava: java.awt.geom.AffineTransform = {
      new java.awt.geom.AffineTransform(
        affine.scaleX,
        affine.shearY,
        affine.shearX,
        affine.scaleY,
        affine.shiftX,
        affine.shiftY)
    }
  }

  implicit class RichStroke(basicStroke: BasicStroke) {
    def update(
      strokeWeight: Option[Double] = None,
      lineStyle: Option[LineStyle] = None): BasicStroke = {
      val newWeight = strokeWeight.fold(basicStroke.getLineWidth)(_.toFloat)
      val newDashPattern = lineStyle.fold(basicStroke.getDashArray) { style =>
        if (style.dashPattern.isEmpty) null // scalastyle:ignore
        else if (style.dashPattern.tail.isEmpty) Array.fill(2)(style.dashPattern.head.toFloat)
        else style.dashPattern.toArray.map(_.toFloat)
      }
      val newDashPhase = lineStyle.fold(basicStroke.getDashPhase)(_.offset.toFloat)

      new BasicStroke(
        newWeight,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_ROUND,
        10.0f,
        newDashPattern,
        newDashPhase
      )
    }
  }

}
