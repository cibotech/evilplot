package com.cibo.evilplot

import java.io.File
import java.nio.file.Paths

import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.ContourPlot
import javax.imageio.ImageIO
import org.scalatest.{FunSpec, Matchers}

class WriteOutDemoPlots extends FunSpec with Matchers {
  import com.cibo.evilplot.plot.aesthetics.DefaultTheme.defaultTheme

  val plots = Seq(
    DemoPlots.crazyPlot -> "crazy",
    DemoPlots.facetedPlot -> "faceted",
    DemoPlots.heatmap -> "heatmap",
    DemoPlots.marginalHistogram -> "marginalhistogram",
    DemoPlots.pieChart -> "piechart",
    DemoPlots.clusteredBarChart -> "clusteredbar",
    DemoPlots.clusteredStackedBarChart -> "clusteredstackedbar",
    DemoPlots.stackedBarChart -> "stackedbar",
    DemoPlots.barChart -> "bar",
    DemoPlots.scatterPlot -> "scatter",
    DemoPlots.functionPlot -> "functionPlot",
    DemoPlots.boxPlot -> "boxPlot",
    DemoPlots.facetedPlot -> "facetedPlot",
    DemoPlots.barChart -> "barChart",
    DemoPlots.histogram -> "histogram"
  )
  private val desktop = s"${System.getenv("HOME")}/Desktop"
  private def procln(s: String): Point = s.split(",").map(_.trim) match {
    case Array(x, y) => Point(x.toDouble, y.toDouble)
  }
  val also: (Drawable, String) = {
    val f = Paths.get(s"$desktop/data.csv").toFile
    val pts = scala.io.Source.fromFile(f).getLines().toSeq.tail.map(procln)
    ContourPlot(pts).standard().render() -> "contour"
  }

  describe("Demo Plots") {
    it("is generated") {
      for { (plot, name) <- plots :+ also } {
        val bi = plot.asBufferedImage
        ImageIO.write(bi,
                      "png",
                      new File(s"$desktop/its/$name.png"))
      }
    }
  }
}
