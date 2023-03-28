package com.cibo.evilplot

import java.io.File
import java.nio.file.{Files, Paths}
import com.cibo.evilplot.demo.DemoPlots

import javax.imageio.ImageIO
import com.cibo.evilplot.geometry.Drawable
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import scala.util.Try

object WriteOutDemoPlots {
  def apply(args:Array[String]):Unit = 
    for(plotName <- args; plot <- DemoPlots.get(Symbol(plotName)))
       plot write new java.io.File(s"$plotName.png")
}
class WriteOutDemoPlots extends AnyFunSpec with Matchers {

  //-- DemoPlot name and ratio of colored pixels (to represent an simple hash)
  val plots = Seq(
    'linePlot -> 0.01498,
    'heatmap -> 0.81790,
    'pieChart -> 0.44209,
    'clusteredBarChart -> 0.31712,
    'clusteredStackedBarChart -> 0.30259,
    'stackedBarChart -> 0.35687,
    'barChart -> 0.18869,
    'functionPlot -> 0.01728,
    'markerPlot -> 0.01008,
    'crazyPlot -> 0.10755,
    'facetedPlot -> 0.04951,
    'marginalHistogram -> 0.04002,
    'scatterPlot -> 0.02314,
    'boxPlot -> 0.29182,
    'facetedPlot -> 0.04951,
    'histogramOverlay -> 0.32281
  )

  val tmpPathOpt = {
    val tmpPath = Paths.get("/tmp/evilplot")
    if (Files.notExists(tmpPath)) Try{Files.createDirectories(tmpPath)}
    if(Files.notExists(tmpPath)) None else {
      println(s"Saving rendered png's to $tmpPath")
      Some(tmpPath)
    }
  }

  describe("Demo Plots") {
    it("render to consistent murmur hash") {
      for { (name, ratioTruth) <- plots; plot <- DemoPlots.get(name)} {

        val bi = plot.asBufferedImage

        def isColored(c:Int):Boolean = {
          val r = (c >> 16) & 0xFF;
          val g = (c >> 8) & 0xFF;
          val b = (c >> 8) & 0xFF;
          r + g + b > 10
        }

        val ratio:Double = {
          val pixels = (for(x <- 0 until bi.getWidth; y <- 0 until bi.getHeight) yield bi.getRGB(x,y)).toArray
          pixels.count(isColored).toDouble/pixels.size
        }

        val delta = math.abs(ratioTruth - ratio)
        println(f"""$name -> $ratio%5.5f, //delta = $delta%8.8f""")
        assert(delta < 0.0015, s"$name out of range $ratio != $ratioTruth")

        //--write img to file if the tmp path is available
        for(_ <- None; tmpPath <- tmpPathOpt){
          val file = new File(s"${tmpPath.toAbsolutePath.toString}/${name.name}.png")
          ImageIO.write(bi, "png", file)
          file.exists() shouldBe true
        }
      }
    }
  }
}
