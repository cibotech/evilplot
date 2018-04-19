package com.cibo.evilplot

import java.io.File
import java.nio.file.Paths

import com.cibo.evilplot.colors._
import com.cibo.evilplot.demo.DemoPlots
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Point
import com.cibo.evilplot.plot.ContourPlot
import javax.imageio.ImageIO
import org.scalatest.{FunSpec, Matchers}

class DemoPlotsSpec extends FunSpec with Matchers {
  import com.cibo.evilplot.plot.aesthetics.DefaultTheme.defaultTheme
  import scala.reflect.runtime.{universe => ru}
  val plots = {
    val tag = ru.typeOf[DemoPlots.type]
    val current = ru.runtimeMirror(getClass.getClassLoader)
    val examplesMirror = current reflect DemoPlots
    val desired: ru.Type = ru.typeOf[Drawable]
    tag.decls.withFilter { field =>
      field.typeSignature.toString.contains("Drawable")
    }.map { x =>
      // TODO: Figure out how to call the lazy vals.
      examplesMirror.reflectField(tag.decl(x.name).asTerm).get.asInstanceOf[Drawable] -> x.name
    }.toSeq
  }
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
