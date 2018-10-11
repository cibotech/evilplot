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

package com.cibo.evilplot

import java.util.UUID

import cats.data.State
import com.cibo.evilplot.colors._
import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.geometry.Clipping.Edge
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.interaction.InteractionMaskContext
import com.cibo.evilplot.numeric.{Point, Point3d}
import com.cibo.evilplot.plot.{CartesianPlot, LinePlot, Overlay, Plot}
import com.cibo.evilplot.plot.renderers.PathRenderer
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.Random

@JSExportTopLevel("EvilPlot")
object EvilPlot {

  /** Render a plot definition to the specified canvas. */
  @JSExport
  def renderEvilPlot(json: String, canvasId: String, width: Double, height: Double): Unit = {
    renderEvilPlot(json, canvasId, Some(Extent(width, height)))
  }

  @JSExport
  def renderEvilPlot(json: String, canvasId: String): Unit = {
    renderEvilPlot(json, canvasId, None)
  }

  def renderEvilPlot(json: String, canvasId: String, size: Option[Extent]): Unit = {
    val definition = JSONUtils.decodeStr[Drawable](json)
    renderEvilPlot(definition, canvasId, size)
  }

  def renderEvilPlot(plot: Drawable, canvasId: String, size: Option[Extent]): Unit = {
    val paddingHack = 20
    val canvas = Utils.getCanvasFromElementId(canvasId)
    val ctx = prepareCanvas(canvas.canvas, plot.extent)
    val paddedSize = Extent(plot.extent.width - paddingHack, plot.extent.height - paddingHack)
    fit(plot padAll paddingHack / 2, paddedSize).draw(CanvasRenderContext(ctx))
  }

  /** Render the example plots to the specified canvas. */
  @JSExport
  def renderExample(canvasId: String): Unit = {
    interactionExample()
    addExample(DemoPlots.simpleGroupedPlot)
    addExample(DemoPlots.simpleContinuousPlot)

    addExample(DemoPlots.simpleCartesianPlot)
    addExample(DemoPlots.densityPlot)
    addExample(DemoPlots.legendFeatures)
    addExample(DemoPlots.axesTesting)
    addExample(DemoPlots.functionPlot)
    addExample(DemoPlots.markerPlot)
    addExample(DemoPlots.scatterPlot)
    addExample(DemoPlots.barChart)
    addExample(DemoPlots.boxPlot)
    addExample(DemoPlots.clusteredBoxPlot)
    addExample(DemoPlots.facetedPlot)
    addExample(DemoPlots.heatmap)
    addExample(DemoPlots.marginalHistogram)
    addExample(DemoPlots.clusteredBarChart)
    addExample(DemoPlots.stackedBarChart)
    addExample(DemoPlots.clusteredStackedBarChart)
  }

  // Crappy way to maintain state
  private var activePoint: Option[Point3d[Int]] = None
  private var hoveredPoint: Option[Int] = None

  def interactionExample() = {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

    val anchors = Seq((10.0, 10.0), (0.0, 10.0), (10.0, 0.0), (0.0, 0.0))
    val data = (Seq.fill(300)(Math.random() * 10, Math.random() * 10) ++ anchors).zipWithIndex.map(x => Point3d[Int](x._1._1, x._1._2, x._2))
    val canvasId = UUID.randomUUID().toString

    val screenWidth = dom.window.innerWidth
    val screenHeight = dom.window.innerHeight

    val ctx = prepareContext(screenWidth, screenHeight)
    val interactionContext = prepareInteractionContext(screenWidth, screenHeight)

    def pointHover(hovered: Point3d[Int]) = {
      if(hovered.z != hoveredPoint.getOrElse(-1)){
        hoveredPoint = Some(hovered.z)
        renderPlot()
      }
    }

    def renderPlot() = {
      dom.window.requestAnimationFrame { _ =>

        val updatedPlot = CartesianPlot(data){
          _.scatter({x: Point3d[Int] =>
            if(hoveredPoint.getOrElse(-1) == x.z) {
              Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(1))
            } else if(activePoint.map(_.z).getOrElse(-1) == x.z){
              Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(4))
            } else Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(2))
          })
        }
        ctx.clear()
        (Text(s"Active Point: ${activePoint.map(_.z)}, Hovered Point: ${hoveredPoint}", size = 16)
          .padBottom(20) above updatedPlot.standard().render()).padAll(10).draw(ctx)
      }
    }

    val plot = CartesianPlot(data){
      _.scatter({x: Point3d[Int] => Interaction(
        Disc(5).filled(colors.DefaultColors.lightPalette(2))
          .translate(-5, -5), OnHover(() => pointHover(x)), OnClick(() => {
            println("Clicked")
            activePoint = Some(x)
            renderPlot()
          })
      )})
    }.standard()

    interactionContext.attachToMainCanvas(ctx.canvas.canvas, defaultMove = { () =>
      hoveredPoint = None
      renderPlot()
    })
    (Text(s"Active Point: ${activePoint.map(_.z)}, Hovered Point: ${hoveredPoint}", size = 16)
      .padBottom(20) above plot.render()).padAll(10).draw(interactionContext)

    renderPlot()
  }

  private def prepareContext(screenWidth: Double, screenHeight: Double, id: String = "" ) = {
    val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    dom.document.body.appendChild(canvas)
    CanvasRenderContext(prepareCanvas(canvas, Extent(screenWidth, screenHeight)))
  }

  private def prepareInteractionContext(screenWidth: Double, screenHeight: Double) = {
    val canvasInteraction = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    InteractionMaskContext(prepareCanvas(canvasInteraction, Extent(screenWidth, screenHeight)))
  }

  private def addExample(plot: Drawable): Unit = {
    val canvasId = UUID.randomUUID().toString
    val screenWidth = dom.window.innerWidth
    val screenHeight = dom.window.innerHeight
    val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    canvas.setAttribute("id", canvasId)
    dom.document.body.appendChild(canvas)
    val ctx = CanvasRenderContext(prepareCanvas(canvas, Extent(screenWidth, screenHeight)))
    plot.padAll(10).draw(ctx)
  }

  def renderPaletteExample(colors: Seq[Color]): Unit = {
    val paletteID = "palette"
    val div = dom.document.getElementById(paletteID)
    colors.foreach { color =>
      val element = dom.document.createElement("div")
      element.setAttribute(
        "style",
        s"width: 40px; " +
          s"height: 40px; " +
          s"display: inline-block;" +
          s"background-color: ${color.repr};")

      div.appendChild(element)
    }
  }

  private def prepareCanvas(
    canvas: HTMLCanvasElement,
    extent: Extent,
    scaleHack: Double = 2.0
  ): CanvasRenderingContext2D = {
    val ctx = canvas.getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.canvas.style.width = extent.width + "px"
    ctx.canvas.style.height = extent.height + "px"
    ctx.canvas.width = (extent.width.toInt * scaleHack).toInt
    ctx.canvas.height = (extent.height.toInt * scaleHack).toInt

    ctx.scale(scaleHack, scaleHack)
    ctx
  }
}
