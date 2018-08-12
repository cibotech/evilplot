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
import com.cibo.evilplot.colors.{Color, DefaultColors, HEX, HTMLNamedColors}
import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.{LinePlot, Overlay}
import com.cibo.evilplot.plot.renderers.PathRenderer
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.HTMLCanvasElement

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}

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
    val ctx = prepareCanvas(canvasId, plot.extent)
    val paddedSize = Extent(plot.extent.width - paddingHack, plot.extent.height - paddingHack)
    fit(plot padAll paddingHack / 2, paddedSize).draw(CanvasRenderContext(ctx))
  }

  /** Render the example plots to the specified canvas. */
  @JSExport
  def renderExample(canvasId: String): Unit = {
    addExample(DemoPlots.densityPlot)
    addExample(DemoPlots.legendFeatures)
    addExample(DemoPlots.axesTesting)
    addExample(DemoPlots.functionPlot)
    addExample(DemoPlots.markerPlot)
    addExample(DemoPlots.marginalHistogram)
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

  private def addExample(plot: Drawable): Unit = {
    val canvasId = UUID.randomUUID().toString
    val screenWidth = dom.window.innerWidth
    val screenHeight = dom.window.innerHeight
    val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    canvas.setAttribute("id", canvasId)
    dom.document.body.appendChild(canvas)
    val ctx = CanvasRenderContext(prepareCanvas(canvasId, Extent(screenWidth, screenHeight)))
    plot.padAll(10).draw(ctx)
  }

  def renderPaletteExample(colors: Seq[Color]): Unit = {
    println("Rendering palette")
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
    id: String,
    extent: Extent
  ): CanvasRenderingContext2D = {
    val ctx = Utils.getCanvasFromElementId(id)
    val canvasResolutionScaleHack = 2

    ctx.canvas.style.width = extent.width + "px"
    ctx.canvas.style.height = extent.height + "px"
    ctx.canvas.width = extent.width.toInt * canvasResolutionScaleHack
    ctx.canvas.height = extent.height.toInt * canvasResolutionScaleHack

    ctx.scale(canvasResolutionScaleHack, canvasResolutionScaleHack)
    ctx
  }
}
