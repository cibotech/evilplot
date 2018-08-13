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

import com.cibo.evilplot.colors._
import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.demo.DemoPlots.plotAreaSize
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.interaction.InteractionMaskContext
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultFonts, DefaultTheme}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{BarRenderer, PointRenderer}
import com.cibo.evilplot.plot.{Bar, BarChart, LegendContext, Plot, ScatterPlot}
import org.scalajs.dom
import org.scalajs.dom.CanvasRenderingContext2D
import org.scalajs.dom.raw.{HTMLCanvasElement, MouseEvent}

import scala.scalajs.js.annotation.{JSExport, JSExportTopLevel}
import scala.util.Random

@JSExportTopLevel("InteractiveEvilPlot")
object InteractiveEvilPlot {

  /* TODO  get rid of these, for demo purposes only*/
  private var selectedBar: Option[Double] = None
  private var pointMouseoverTarget: Option[Double] = None

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
    addAnimatedChart()
  }

  def animatedBarChart(ms: Double, ctx: CanvasRenderContext, mouseContext: InteractionMaskContext): Unit = {
    implicit val theme: Theme = DefaultTheme.copy(
      fonts = DefaultFonts
        .copy(tickLabelSize = 14, legendLabelSize = 14, fontFace = "'Lato', sans-serif")
    )

    val magnitude: Double = (ms - 3000) / 3000

    val percentChange = Seq[Double](-10, 5, 12, 68, -22).map(x => x * magnitude.min(1.0))

    val labels = Seq("one", "two", "three", "four", "five")

    def labeledByColor(implicit theme: Theme) = new BarRenderer {
      def render(plot: Plot, extent: Extent, category: Bar): Drawable = {
        val value = category.values.head
        val rect = Rect(extent).copy(onClick = Some(() => dom.window.alert(s"${value} %")),
          onMouseover = Some(() => {
            println(s"MOUSE OVER ${value}")
            selectedBar = Some(value)
          }))

        val positive = HEX("#4c78a8")
        val negative = HEX("#e45756")
        val color = if (selectedBar.exists( x => x.toInt == value.toInt)) {
          HEX("f68619")
        } else if (value >= 0) positive else negative

        val text = if (selectedBar.exists( x => x.toInt == value.toInt)) {
          s"${value.toInt}%"
        } else ""

        Align
          .center(
            rect filled color,
            Text(text, fontFace = theme.fonts.fontFace, size = 20).filled(theme.colors.label))
          .group
      }
    }

    val chart = BarChart
      .custom(percentChange.map(Bar.apply), spacing = Some(20), barRenderer = Some(labeledByColor))
      .standard(xLabels = labels)
      .ybounds(-25.0, 75.0)
      .hline(0)

    val drawable = chart.render(DemoPlots.plotAreaSize).padAll(10)

    drawable.draw(ctx)
    drawable.draw(mouseContext)

  }

  val points = Seq.fill(150)(Point(Random.nextDouble(), Random.nextDouble())) :+ Point(0.0, 0.0)
  val years = Seq.fill(150)(Random.nextDouble()) :+ 1.0

  def interactiveScatterPlot(x: Double, ctx: CanvasRenderContext, mouseContext: InteractionMaskContext): Unit = {
    implicit val theme: Theme = DefaultTheme.copy(
      fonts = DefaultFonts
        .copy(tickLabelSize = 14, legendLabelSize = 14, fontFace = "'Lato', sans-serif")
    )

    val coloring = ContinuousColoring
      .gradient3(
        RGB(26, 188, 156),
        RGB(46, 204, 113),
        RGB(52, 152, 219))

    val depths = years
    val plot = ScatterPlot(
      points,
      pointRenderer = Some(
        new PointRenderer {
          private val useColoring = coloring
          private val colorFunc = useColoring(depths)
          private val radius = theme.elements.pointSize

          def render(plot: Plot, extent: Extent, index: Int): Drawable = {
            Disc(radius, onMouseover = Some({ () =>
              pointMouseoverTarget = Some(depths(index))
            })).translate(-radius, -radius).filled(colorFunc(depths(index)))
          }

          override def legendContext: LegendContext =
            useColoring.legendContext(depths)
        })
    ).standard()
      .xLabel("x")
      .yLabel("y")
      .trend(1, 0)
      .rightLegend().padTop(10)


    val drawable = {if(pointMouseoverTarget.isDefined){
      plot.bottomLabel(s"Value: ${pointMouseoverTarget.get}")
    } else {
     plot.bottomLabel("")
    }}.render(plotAreaSize)

    drawable.draw(ctx)
    drawable.draw(mouseContext)

  }

  def addAnimatedChart(): Unit = {

    val canvasId = UUID.randomUUID().toString
    val screenWidth = dom.window.innerWidth
    val screenHeight = dom.window.innerHeight
    val canvas = dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement]
    canvas.setAttribute("id", canvasId)
    dom.document.body.appendChild(canvas)
    val ctx = CanvasRenderContext(prepareCanvas(canvasId, Extent(screenWidth, screenHeight)))
    val mouseContext = InteractionMaskContext(prepareCanvas("virtual", Extent(screenWidth, screenHeight)))

    ctx.canvas.canvas.addEventListener[MouseEvent]("click", { x =>
      val canvasY = x.clientY - ctx.canvas.canvas.getBoundingClientRect().top
      val canvasX = x.clientX - ctx.canvas.canvas.getBoundingClientRect().left
      mouseContext.events(canvasX, canvasY).foreach(_.click.foreach(_()))
    })

    ctx.canvas.canvas.addEventListener[MouseEvent]("mousemove", { x =>
      val canvasY = x.clientY - ctx.canvas.canvas.getBoundingClientRect().top
      val canvasX = x.clientX - ctx.canvas.canvas.getBoundingClientRect().left
      mouseContext.events(canvasX, canvasY).flatMap(_.mouseover.map(_())).getOrElse{
        selectedBar = None
        pointMouseoverTarget = None
      }
    })

    renderAnim(ctx, mouseContext)
  }

  /*
    These are some significant performance enhancements that need to take place.
    - Separate animation layers
    - Stop render cycle when animation is not in progress
    - Separate re-render for interaction
   */

  def renderAnim(ctx: CanvasRenderContext, mouse: InteractionMaskContext): Int = {
    dom.window.requestAnimationFrame { msSinceInit =>
      ctx.canvas.clearRect(0, 0, ctx.canvas.canvas.width, ctx.canvas.canvas.height)
      mouse.clearEventListeners()
      interactiveScatterPlot(msSinceInit, ctx, mouse)

      renderAnim(ctx, mouse)
    }
  }


  private def prepareCanvas(
    id: String,
    extent: Extent
  ): CanvasRenderingContext2D = {
    val ctx = if(id == "virtual"){
      dom.document.createElement("canvas").asInstanceOf[HTMLCanvasElement].getContext("2d")
        .asInstanceOf[dom.CanvasRenderingContext2D]
    } else {
      Utils.getCanvasFromElementId(id)
    }


    val canvasResolutionScaleHack = 2

    ctx.canvas.style.width = extent.width + "px"
    ctx.canvas.style.height = extent.height + "px"
    ctx.canvas.width = extent.width.toInt * canvasResolutionScaleHack
    ctx.canvas.height = extent.height.toInt * canvasResolutionScaleHack

    ctx.scale(canvasResolutionScaleHack, canvasResolutionScaleHack)
    ctx
  }
}
