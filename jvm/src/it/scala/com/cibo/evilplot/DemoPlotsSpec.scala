package com.cibo.evilplot

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.colors._
import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.geometry.Graphics2DRenderContext
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.ScatterPlot
import com.cibo.evilplot.plot.renderers.PointRenderer
import org.scalatest.{FunSpec, Matchers}

class DemoPlotsSpec extends FunSpec with Matchers {
  val pathColor = HTMLNamedColors.blue
  val fillColor = HTMLNamedColors.white
  val strokeWidth = 4
  val topWhisker = 100
  val upperToMiddle = 40
  val middleToLower = 30
  val bottomWhisker = 30
  val width = 50
  val t = Align.center(
    Line(topWhisker, strokeWidth).rotated(90).colored(pathColor),
    BorderRect.filled(width, upperToMiddle).colored(pathColor).filled(fillColor),
    BorderRect.filled(width, middleToLower).colored(pathColor).filled(fillColor),
    Line(bottomWhisker, strokeWidth).rotated(90).colored(pathColor)
  ).reduce(_ above _)
  import scala.util.Random.nextDouble
  val myData = Seq.fill(100)(Point(nextDouble(), nextDouble()))
  val scatterPlot = ScatterPlot(myData, pointRenderer = PointRenderer.default(size = 3))

  lazy val plots = Seq(t -> "name",
    scatterPlot.render() -> "scatter",
    scatterPlot.xAxis().yAxis().xGrid().yGrid().background().render() -> "withacc"
  )



//    DemoPlots.yieldScatterPlot -> "scatter",
//    DemoPlots.boxPlotRmResiduals -> "boxplot",
//    DemoPlots.contourPlot -> "contour",
//    DemoPlots.histogramPlot -> "histogram",
//    DemoPlots.crazyPlot -> "crazy",
//    DemoPlots.facetedPlot -> "faceted",
//    DemoPlots.heatmap -> "heatmap",
//    DemoPlots.marginalHistogram -> "marginalhistogram",
//    DemoPlots.pieChart -> "piechart",
//    DemoPlots.clusteredBarChart -> "clusteredbar",
//    DemoPlots.clusteredStackedBarChart -> "clusteredstackedbar",
//    DemoPlots.stackedBarChart -> "stackedbar",
//    DemoPlots.barChart -> "bar"

  describe("Demo Plots") {
    it("is generated") {
      for { (plot, name) <- plots } {
        val bi = plot.asBufferedImage
        ImageIO.write(bi, "png", new File(s"/Users/zgraziano/Desktop/its/$name.png"))
      }
    }
  }
}
