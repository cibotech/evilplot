package com.cibo.evilplot

import java.io.File
import java.nio.file.{Files, Paths}

import com.cibo.evilplot.demo.DemoPlots
import javax.imageio.ImageIO
import org.scalatest.{FunSpec, Matchers}

class WriteOutDemoPlots extends FunSpec with Matchers {

  val plots = Seq(
    DemoPlots.linePlot -> "linePlot",
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
    DemoPlots.markerPlot -> "markerPlot",
    DemoPlots.boxPlot -> "boxPlot",
    DemoPlots.facetedPlot -> "facetedPlot"
  )

  val tmpPath = Paths.get("/tmp/evilplot")
  if (Files.notExists(tmpPath)) Files.createDirectories(tmpPath)

  describe("Demo Plots") {
    it("is generated") {
      for { (plot, name) <- plots } {
        val bi = plot.asBufferedImage
        ImageIO.write(bi, "png", new File(s"${tmpPath.toAbsolutePath.toString}/$name.png"))
      }
    }
  }
}
