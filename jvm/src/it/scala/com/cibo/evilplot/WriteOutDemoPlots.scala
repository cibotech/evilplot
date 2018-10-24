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
    'linePlot -> "2c7176c7",
    'heatmap -> "6bf94558",
    'pieChart -> "7e2b46b0",
    'clusteredBarChart -> "cf8c82f8",
    'clusteredStackedBarChart -> "a3dd008b",
    'stackedBarChart -> "1285ec66",
    'barChart -> "2f7a0025",
    'functionPlot -> "b5814ac0",
    'markerPlot -> "9ffe947c",
    'crazyPlot -> "c67f3ca1",
    'facetedPlot -> "0b80a76d",
    'marginalHistogram -> "d4a4ec4b",
    'scatterPlot -> "a8467f56",
    'boxPlot -> "89fae720",
    'facetedPlot -> "0b80a76d",
    'histogramOverlay -> "e1ed96ca"
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

        def rgb(c:Int) = {
          val r = (c >> 16) & 0xFF;
          val g = (c >> 8) & 0xFF;
          val b = (c >> 8) & 0xFF;
          (r,g,b)
        }
        def isColored(c:Int):Boolean = {
          val (r,g,b) = rgb(c)
          r + g + b > 10
        }

        //--hashValue of the image
        val hashValue:String = {
          def hex(i:Int):String = "%08x" format i
          val pixels = (for(x <- 0 until bi.getWidth; y <- 0 until bi.getHeight) yield bi.getRGB(x,y)).toArray
          val mono = pixels.map{p => if(isColored(p)) 0 else 1}
          val coloredRatio = pixels.count(isColored).toDouble/pixels.size
          val monommr = hex(scala.util.hashing.MurmurHash3.arrayHash(mono))
          val mmr = hex(scala.util.hashing.MurmurHash3.arrayHash(pixels))
          // int javaBlue = (javaRGB >> 0) & 0xFF;
          val xor = hex(pixels.reduce{_ ^ _})
          println(f"${name.name}%-30s monommr:$monommr mmr:$mmr xor:$xor w:${bi.getWidth} h:${bi.getHeight} coloredRatio:$coloredRatio")
          mmr
        }

        println(s"""$name -> "$hashValue",""")

        s"$name-$hashValue" shouldBe s"$name-$hashValueTruth"


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
