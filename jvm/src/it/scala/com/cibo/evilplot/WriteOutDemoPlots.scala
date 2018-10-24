package com.cibo.evilplot

import java.io.File
import java.nio.file.{Files, Paths}

import com.cibo.evilplot.demo.DemoPlots
import javax.imageio.ImageIO
import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.geometry.Drawable
import scala.util.Try

object WriteOutDemoPlots {
  def apply(args:Array[String]):Unit = 
    for(plotName <- args; plot <- DemoPlots.get(Symbol(plotName)))
       plot write new java.io.File(s"$plotName.png")
}
class WriteOutDemoPlots extends FunSpec with Matchers {

  val plots = Seq(
    'linePlot -> "9fa9d6fb",
    'heatmap -> "8eb17253",
    'pieChart -> "e470690b",
    'clusteredBarChart -> "0214ab04",
    'clusteredStackedBarChart -> "c7045d0a",
    'stackedBarChart -> "bf508e8c",
    'barChart -> "8102586e",
    'functionPlot -> "c537877d",
    'markerPlot -> "f95778db",
    'crazyPlot -> "3ff2d020",
    'facetedPlot -> "8f4ce32d",
    'marginalHistogram -> "6acf977e",
    'scatterPlot -> "9abab52b",
    'boxPlot -> "ec53bbdc",
    'facetedPlot -> "8f4ce32d",
    'histogramOverlay -> "3699c648"
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
      for { (name, hashValueTruth) <- plots; plot <- DemoPlots.get(name)} {

        val bi = plot.asBufferedImage

        //--hashValue of the image
        val hashValue:String = {
          val pixels = for(x <- 0 until bi.getWidth; y <- 0 until bi.getHeight) yield bi.getRGB(x,y)
          "%08x" format pixels.toVector.## //use scala's built in murmurhash function
        }

        s"$name-$hashValue" shouldBe s"$name-$hashValueTruth"

        println(s"""$name -> "$hashValue",""")

        //--write img to file if the tmp path is available
        for(_ <- None; tmpPath <- tmpPathOpt){
          val file = new File(s"${tmpPath.toAbsolutePath.toString}/${name.name}.png")
          // println(s"Write ${name.name} to $file")
          ImageIO.write(bi, "png", file)
          file.exists() shouldBe true
        }
      }
    }
  }
}
