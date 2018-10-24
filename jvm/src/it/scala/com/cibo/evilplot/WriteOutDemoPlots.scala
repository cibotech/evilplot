package com.cibo.evilplot

import java.io.File
import java.nio.file.{Files, Paths}

import com.cibo.evilplot.demo.DemoPlots
import javax.imageio.ImageIO
import org.scalatest.{FunSpec, Matchers}
import com.cibo.evilplot.geometry.Drawable

class WriteOutDemoPlots extends FunSpec with Matchers {

  val plots = Seq(
    'linePlot -> "fca0af7afa",
    'heatmap -> "402b0f28e0",
    'pieChart -> "de8f6d6d44",
    'clusteredBarChart -> "00ceb43ff3",
    'clusteredStackedBarChart -> "015225d567",
    'stackedBarChart -> "f0978d9322",
    'barChart -> "003f69c9c6",
    'functionPlot -> "d0692b3eff",
    'markerPlot -> "736c4b7471",
    'crazyPlot -> "88a86f0074",
    'facetedPlot -> "6f5f2fc577",
    'marginalHistogram -> "312e547f8f",
    'scatterPlot -> "ffeb4cb0e5",
    'boxPlot -> "9d5487bc54",
    'facetedPlot -> "6f5f2fc577",
    'histogramOverlay -> "821be14621"
  )

  val demoPlotMethods= DemoPlots.getClass.getMethods.map{m => Symbol(m.getName) -> m}.toMap

  val tmpPath = Paths.get("/tmp/evilplot")
  if (Files.notExists(tmpPath)) Files.createDirectories(tmpPath)

  describe("Demo Plots") {
    it("is generated") {
      for { (name, sha1Truth) <- plots } {

        scala.util.Random.setSeed(666L) //evil global seed renewed for each plot render
        val plot = demoPlotMethods(name).invoke(DemoPlots).asInstanceOf[Drawable]

        val bi = plot.asBufferedImage

        //--sha1 of the image
        val sha1:String = {
          val baos = new java.io.ByteArrayOutputStream();
          ImageIO.write(bi, "png", baos);
          val md = java.security.MessageDigest.getInstance("SHA-1")
          val sha1Bytes = md.digest(baos.toByteArray())
          sha1Bytes map {"%02x" format _} mkString "" take 10
        }

        s"$name-$sha1" shouldBe s"$name-$sha1Truth"

        val file = new File(s"${tmpPath.toAbsolutePath.toString}/${name.name}.png")
        println(s"""$name -> "$sha1",""")

        //--write img to file
        ImageIO.write(bi, "png", file)
        file.exists() shouldBe true
      }
    }
  }
}
