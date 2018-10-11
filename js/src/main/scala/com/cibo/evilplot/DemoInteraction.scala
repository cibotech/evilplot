package com.cibo.evilplot

import java.util.UUID

import com.cibo.evilplot.geometry.{CanvasRenderContext, Disc, Interaction, OnClick, OnHover, Text}
import com.cibo.evilplot.interaction.CanvasInteractionContext
import com.cibo.evilplot.numeric.Point3d
import com.cibo.evilplot.plot.CartesianPlot
import org.scalajs.dom

object DemoInteraction {
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
      dom.window.requestAnimationFrame { _ =>

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
      println("Clicked")
      activePoint = Some(point3d)
      renderPlot() // rerender
    }

    // define default move, to clear hovered point if there is none being hovered
    val defaultMove: () => Unit = () => {
      hoveredPoint = None
      renderPlot() // rerender
    }

    // Initial plot
    val plot = CartesianPlot(data){
      _.scatter({x: Point3d[Int] => Interaction( // attach interaction events, in non interaction context, this will be ignored
        Disc(5).filled(colors.DefaultColors.lightPalette(2))
          .translate(-5, -5), OnHover(() => onHover(x)), OnClick(() => onClick(x))
      )})
    }.standard()

    //Attach event handlers to the canvas that is displayed
    interactionMaskContext.attachToMainCanvas(ctx.canvas.canvas, defaultMove = defaultMove)

    //Render the "virtual" interaction mask
    (Text(s"Active Point: ${activePoint.map(_.z)}, Hovered Point: ${hoveredPoint.map(_.z)}", size = 16)
      .padBottom(20) above plot.render()).padAll(10).draw(interactionMaskContext)

    //Render displayed plot
    renderPlot()
  }
}
