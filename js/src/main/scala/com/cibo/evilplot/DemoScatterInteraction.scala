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

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.demo.DemoPlots.plotAreaSize
import com.cibo.evilplot.geometry.{CanvasRenderContext, Disc, Drawable, Extent, IEInfo, Interaction, OnClick, OnHover, Text}
import com.cibo.evilplot.interaction.CanvasInteractionContext
import com.cibo.evilplot.numeric.{Point, Point3d}
import com.cibo.evilplot.plot.CartesianPlot
import org.scalajs.dom

object DemoScatterInteraction {
  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  // Crappy way to maintain state
  private var activePoint: Option[Point3d[Int]] = None
  private var hoveredPoint: Option[Point3d[Int]] = None

  def scatter(ctx: CanvasRenderContext, interactionMaskContext: CanvasInteractionContext, screenWidth: Double, screenHeight: Double) = {

    val anchors = Seq((10.0, 10.0), (0.0, 10.0), (10.0, 0.0), (0.0, 0.0))
    val data = (Seq.fill(300)(Math.random() * 10, Math.random() * 10) ++ anchors).zipWithIndex.map(x => Point3d[Int](x._1._1, x._1._2, x._2))
    val canvasId = UUID.randomUUID().toString

    // Rerender plot to draw updated state
    def renderPlot() = {
      dom.window.requestAnimationFrame { d: Double =>

        val updatedPlot = CartesianPlot(data){
          _.scatter({x: Point3d[Int] =>
            if(hoveredPoint.map(_.z).getOrElse(-1) == x.z) {  // if hovered

              Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(1))
            } else if(activePoint.map(_.z).getOrElse(-1) == x.z){  // if active

              Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(4))
            } else Disc(5).translate(-5, -5).filled(colors.DefaultColors.lightPalette(2))
          })
        }

        // Clear the canvas, otherwise new rendering will overlay with old
        ctx.clear()
        (Text(s"Active Point: ${activePoint.map(_.z)}, Hovered Point: ${hoveredPoint.map(_.z)}", size = 16)
          .padBottom(20) above updatedPlot.standard().render()).padAll(10).draw(ctx)
      }
    }

    def onHover(point3d: Point3d[Int]) = {
      if(point3d.z != hoveredPoint.getOrElse(-1)){
        hoveredPoint = Some(point3d)
        renderPlot() // rerender
      }
    }

    def onClick(point3d: Point3d[Int]): Unit ={
      activePoint = Some(point3d)
      renderPlot() // rerender
    }

    // define default move, to clear hovered point if there is none being hovered
    val defaultMove: IEInfo => Unit = _ => {
      hoveredPoint = None
      renderPlot() // rerender
    }

    // Initial plot
    val plot = CartesianPlot(data){
      _.scatter({x: Point3d[Int] => Interaction( // attach interaction events, in non interaction context, this will be ignored
        Disc(5).filled(colors.DefaultColors.lightPalette(2))
          .translate(-5, -5), OnHover(_ => onHover(x)), OnClick{ info =>
            println("inner location", info.innerLocation.x, info.innerLocation.y)
            println("client location", info.clientLocation.x, info.clientLocation.y)
            onClick(x)
         }
      )})
    }.standard()

    //Attach event handlers to the canvas that is displayed
    interactionMaskContext.attachToMainCanvas(ctx.canvas.canvas, defaultMove = defaultMove, mouseLeaveCanvas = _ => println("Mouse left canvas"))

    //Render the "virtual" interaction mask
    (Text(s"Active Point: ${activePoint.map(_.z)}, Hovered Point: ${hoveredPoint.map(_.z)}", size = 16)
      .padBottom(20) above plot.render()).padAll(10).draw(interactionMaskContext)

    //Render displayed plot
    renderPlot()
  }
}

object DemoAreaInteraction {
  import com.cibo.evilplot.plot.aesthetics.DefaultTheme._

  // Crappy way to maintain state
  private var hoveredUpper: Boolean = false
  private var hoveredLower: Boolean = false

  def area(ctx: CanvasRenderContext, interactionMaskContext: CanvasInteractionContext, screenWidth: Double, screenHeight: Double) = {

    val canvasId = UUID.randomUUID().toString

    val xs = Range.BigDecimal(0, 10, 0.1).map(_.toDouble)
    val points = xs.map(x => Point(x, Math.cos(x)))
    val pointslower = xs.map(x => Point(x, Math.cos(x) - 0.15))
    val pointsUpper = xs.map(x => Point(x, Math.cos(x) + 0.15))


    def plotFromData(middle: Seq[Point], upper: Seq[Point], lower: Seq[Point], plotExtent: Extent): Drawable = CartesianPlot(middle)(
      _.line(color = HTMLNamedColors.white),
      _.appendDataAndClosePath(upper.reverse).withPathInteraction(Seq(OnHover(_ => onHoverUpper()))).areaSelfClosing({
        if(hoveredUpper) HTMLNamedColors.fireBrick else HTMLNamedColors.gray
      }),
      _.appendDataAndClosePath(lower.reverse).withPathInteraction(Seq(OnHover(_ => onHoverLower()))).areaSelfClosing({
        if(hoveredLower) HTMLNamedColors.darkGreen else HTMLNamedColors.gray
      })
    ).standard()
      .xLabel("x")
      .yLabel("y")
      .rightLegend()
      .render(plotExtent)

    // Rerender plot to draw updated state
    def renderPlot() = {
      dom.window.requestAnimationFrame { d: Double =>

        val updatedPlot = plotFromData(points, pointsUpper, pointslower, plotAreaSize)

        // Clear the canvas, otherwise new rendering will overlay with old
        ctx.clear()
        updatedPlot.draw(ctx)
      }
    }

    def onHoverUpper() = {
      hoveredUpper = true
      hoveredLower = false
      renderPlot()
    }

    def onHoverLower() = {
      hoveredUpper = false
      hoveredLower = true
      renderPlot()
    }

    // define default move, to clear hovered point if there is none being hovered
    val defaultMove: IEInfo => Unit = _ => {
      hoveredUpper = false
      hoveredLower = false
      renderPlot() // rerender
    }


    // Initial plot
    val initialPlot = plotFromData(points, pointsUpper, pointslower, plotAreaSize)

    //Attach event handlers to the canvas that is displayed
    interactionMaskContext.attachToMainCanvas(ctx.canvas.canvas, defaultMove = defaultMove, mouseLeaveCanvas = _ => println("Mouse left canvas"))

    //Render the "virtual" interaction mask
    initialPlot.draw(interactionMaskContext)

    //Render displayed plot
    renderPlot()
  }
}
