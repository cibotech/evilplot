package example
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, html}
import DSL._

import scala.util.Random
import Colors.{Color, Grey}

case class Point(x: Double, y: Double)

@JSExport
object ScalaJSExample {

  private def fullscreenAndHiRes(ctx: CanvasRenderingContext2D): Unit = {

    val canvasResolutionScaleHack = 3
    val screenWidth = dom.window.innerWidth.toInt
    val screenHeight = dom.window.innerHeight.toInt

    ctx.canvas.style.width = screenWidth + "px"
    ctx.canvas.style.height = screenHeight + "px"
    ctx.canvas.width = screenWidth * canvasResolutionScaleHack
    ctx.canvas.height = screenHeight * canvasResolutionScaleHack

    ctx.scale(canvasResolutionScaleHack, canvasResolutionScaleHack)
  }

  private def createGridLines(maxHeight: Double, width: Double): Renderable =
    DistributeV {
      val lineEveryXUnits     = 40
      val lineThick           = 0.25
      val textHeight          = Text.defaultSize
      val labelFloatAboveLine = 2

      val countOfGridLines = (maxHeight / lineEveryXUnits).toInt

      Seq.tabulate(countOfGridLines){ x =>
        val yHeightLabel = (maxHeight / lineEveryXUnits - x) * lineEveryXUnits

        Pad(bottom = lineEveryXUnits - lineThick){

          val label = Translate(y = -textHeight - labelFloatAboveLine){
            Text(yHeightLabel) filled Grey
          }

          Line(width, lineThick) behind label
        }
      }
    }

  private def yAxis(maxValue: Double, textAndPadHeight: Double, labelTicks: Boolean = false): Renderable = {

    val figureWidth = maxValue
    val tickThick = figureWidth * 0.0025
    val tickLength: Double = figureWidth * 0.025
    val textSize = (12 / 300.0) * maxValue
    val labelEveryKTicks = 5

    val spacingIfTenTicks = maxValue / 10D
    // round to nearest multiple of 5 in the scale
    val fiveInTheScale = maxValue / 20.0 // TODO Is this sane????
    val interTickDist = math.min((spacingIfTenTicks / fiveInTheScale).toInt, 1) * fiveInTheScale

    val numTicks = (maxValue / interTickDist).floor.toInt
    val ticks = Seq.tabulate(numTicks + 1){ i =>
      val tick = Line(tickLength, tickThick) rotated 90 padRight (interTickDist - tickThick)

      tick
//      if( i % labelEveryKTicks == 0 ) tick beside Text(i * interTickDist) rotated 90 else tick
    }.distributeH

    Line(maxValue, tickThick * 2) behind ticks titled ("Awesomeness", textSize) rotated -90 padTop (textAndPadHeight - interTickDist)
  }

  private def createBars(heights: Seq[Double], colors: Seq[Color]) = {
    val barWidth = 50
    val barSpacing = 5

      Align.bottomSeq {
        val rects = heights.map { h => Rect(barWidth, h) titled h.toString}
        rects.zip(colors).map { case (rect, color) => rect filled color labeled color.repr }
      }.distributeH(barSpacing)
  }

  def createBarGraph(size: Extent, data: Seq[Double], colors: Seq[Color]): Renderable = {

    val tickThick = 0.5
    val textAndPadHeight = Text.defaultSize + 5 + tickThick / 2D // text size, label pad, stroke width

    val barChart = Fit(size){
      val justBars = createBars(data, colors)
      val yAx = yAxis(data.max, textAndPadHeight)
      val grid = createGridLines(data.max, justBars.extent.width) padTop (textAndPadHeight - tickThick / 2D)

      (grid --> yAx.extent.width) behind (yAx beside justBars)
    }

    barChart titled ("A Swanky BarChart", 20) padAll 10
  }

  def createScatterPlot(graphSize: Extent, data: Seq[Point]) = {

    val minX = data.map(_.x).min
    val minY = data.map(_.y).min

    val pointSize = {
      val maxX = data.map(_.x).max
      val maxY = data.map(_.y).max
      math.min(maxX, maxY) / 100.0
    }

    val fitScatter = FlipY(Fit(graphSize){
      val scatter = data.map{ case Point(x, y) => Disc(pointSize, x - math.min(0, minX), y - math.min(0, minY)) }.group
      FlipY(yAxis(scatter.extent.height, 0)) beside scatter
    })

    fitScatter titled ("A Scatter Plot", 20) padAll 10
  }

  def createPieChart(scale: Int, data: Seq[Double]) = {
    val pieWedges = {
      val labelPad = 10 // TODO: should be maxTextWidth?

      // cumulativeRotate is complicated b/c we draw wedges straddling the X axis, but that makes labels easier
      val cumulativeRotate = data.map(_ / 2).sliding(2).map(_.sum).scanLeft(0D)(_+_).toVector
      val wedges = data.zip(cumulativeRotate).map { case (frac, cumRot) =>

        val rotate = 360 * cumRot
        val wedge = UnsafeRotate(rotate)(Wedge(360 * frac, scale))
        val label =
          UnsafeRotate(rotate) {
            val text = {
              val baseText = Text(frac.toString) filled Colors.Black
              if (rotate > 90 && rotate < 270) baseText --> (-baseText.extent.width - labelPad) else baseText // TODO: same as left aligned txt?
            }
            val spacer = Disc(scale) filled Colors.Clear padRight labelPad

            DistributeH(Align.middle(spacer, UnsafeRotate(-rotate)(text) ))
          }

        wedge behind label
      }

      wedges.zip(Colors.stream).map { case (r, color) => r filled color }
    }.group

    val legend = FlowH(
      data.zip(Colors.stream).map{ case (d, c) => Rect(scale / 5.0) filled c labeled f"${d*100}%.1f%%" },
      pieWedges.extent
    ) padTop 20

    pieWedges padAll 15 above legend titled("A Smooth Pie Chart", 20) padAll 10
  }

  @JSExport
  def main(canvasDiv: html.Canvas): Unit = {

    import Colors._

    val ctx: CanvasRenderingContext2D = canvasDiv.getContext("2d")
                    .asInstanceOf[CanvasRenderingContext2D]

    fullscreenAndHiRes(ctx)

    val plotAreaSize = Extent(300, 300)

    val barGraph = {
      val colors = Seq(Red, Green, Blue)
      val data = Seq(10D, 100D, 200D)

      createBarGraph(plotAreaSize, data, colors)
    }

    val scatterPlotGraph = {
      val scale = 100
      val scatterData = Seq.tabulate(50){ i =>
        val x = Random.nextDouble() * scale
        Point(x, x + scale * (Random.nextDouble()/2.0 - 0.5))
      }

      createScatterPlot(plotAreaSize, scatterData)
    }

    val pieChart = {
      val scale = 100
      val data = Seq(.3, .2, .1, .3, .1)

      createPieChart(scale, data)
    }

    DistributeH(Align.middle(pieChart, barGraph, scatterPlotGraph)).render(ctx)
  }
}

object Colors {

  // TODO: this needs work
  def stream = {
    val hueSpan = 7
    Stream.from(0).map{ i =>
      // if hueSpan = 8, for instance:
      // Epoch 0 ->  8 equally spaced  0 -  7
      // Epoch 1 -> 16 equally spaced  8 - 21
      // Epoch 2 -> 32 equally spaced 22 - 53
      // Epoch 3 -> 64 equally spaced 54 - 117
      // Epoch 4 -> 128 equally spaced 118 - 245
      // ..
      // e^2 * 8
      // pt = sum_epoch( 8 * 2 ^ (e) ) - log(8)/log(2) // not quite right this last term?
      // pt = 8 * 2 ^ (2) + 8 * 2 ^ (1) + 8 * 2 ^ (0) - 3
      // pt = 8 * (2 ^ (2) + 2 ^ (1) + 2 ^ (0)) - 3
      // pt = 8 * (2^(e+1) - 1) - 3

      import math._
      def log2(x: Double) = log(x) / log(2)
      val magicFactor = log2(hueSpan) // TODO: this may or may not be correct for other hueSpan's
      val epoch = if( i < hueSpan ) 0 else ceil(log2(((i + magicFactor) / hueSpan) + 1) - 1).toInt

      def endIndexOfThisEpoch(e: Int) = 8 * (pow(2,(e + 1)) - 1) - magicFactor

      val slicesThisEpoch = hueSpan * Math.pow(2, epoch)
      val initialRotate = 360.0 / slicesThisEpoch / 2.0

      val zeroBasedIndexInThisEpoch = i - endIndexOfThisEpoch(epoch - 1) - 1

      val saturationDropoff = 2
      def saturationLevel(e: Int) = 100 * 1.0 / pow(saturationDropoff, epoch + 1)
      val saturationBase = 50//100 - saturationLevel(0)
      HSL(
        abs(round(initialRotate + 360.0 / slicesThisEpoch * zeroBasedIndexInThisEpoch).toInt % 360),
        (saturationBase + saturationLevel(epoch)).round.toInt,
        50
      )
    }
  }

  sealed trait Color {
    val repr: String
  }

  case class HSL(hue: Int, saturation: Int, lightness: Int) extends Color {
    require(hue        >= 0 && hue        <  360,        s"hue must be within [0, 360) {was $hue}")
    require(saturation >= 0 && saturation <= 100, s"saturation must be within [0, 100] {was $saturation}")
    require(lightness  >= 0 && lightness  <= 100,  s"lightness must be within [0, 100] {was $lightness}")

    val repr = s"hsl($hue, $saturation%, $lightness%)"
  }

  case object Clear extends Color {
    val repr: String = "rgba(0,0,0,0)"
  }

  sealed abstract class NamedColor(val repr: String) extends Color
  case object AliceBlue             extends NamedColor("aliceblue")
  case object AntiqueWhite          extends NamedColor("antiquewhite")
  case object Aqua                  extends NamedColor("aqua")
  case object Aquamarine            extends NamedColor("aquamarine")
  case object Azure                 extends NamedColor("azure")
  case object Beige                 extends NamedColor("beige")
  case object Bisque                extends NamedColor("bisque")
  case object Black                 extends NamedColor("black")
  case object BlanchedAlmond        extends NamedColor("blanchedalmond")
  case object Blue                  extends NamedColor("blue")
  case object BlueViolet            extends NamedColor("blueviolet")
  case object Brown                 extends NamedColor("brown")
  case object Burlywood             extends NamedColor("burlywood")
  case object CadetBlue             extends NamedColor("cadetblue")
  case object Chartreuse            extends NamedColor("chartreuse")
  case object Chocolate             extends NamedColor("chocolate")
  case object Coral                 extends NamedColor("coral")
  case object CornflowerBlue        extends NamedColor("cornflowerblue")
  case object Cornsilk              extends NamedColor("cornsilk")
  case object Crimson               extends NamedColor("crimson")
  case object Cyan                  extends NamedColor("cyan")
  case object DarkBlue              extends NamedColor("darkblue")
  case object DarkCyan              extends NamedColor("darkcyan")
  case object DarkGoldenrod         extends NamedColor("darkgoldenrod")
  case object DarkGray              extends NamedColor("darkgray")
  case object DarkGreen             extends NamedColor("darkgreen")
  case object DarkGrey              extends NamedColor("darkgrey")
  case object DarkKhaki             extends NamedColor("darkkhaki")
  case object DarkMagenta           extends NamedColor("darkmagenta")
  case object DarkOliveGreen        extends NamedColor("darkolivegreen")
  case object DarkOrange            extends NamedColor("darkorange")
  case object DarkOrchid            extends NamedColor("darkorchid")
  case object DarkRed               extends NamedColor("darkred")
  case object DarkSalmon            extends NamedColor("darksalmon")
  case object DarkSeagreen          extends NamedColor("darkseagreen")
  case object DarkSlateBlue         extends NamedColor("darkslateblue")
  case object DarkSlateGray         extends NamedColor("darkslategray")
  case object DarkSlateGrey         extends NamedColor("darkslategrey")
  case object DarkTurquoise         extends NamedColor("darkturquoise")
  case object DarkViolet            extends NamedColor("darkviolet")
  case object DeepPink              extends NamedColor("deeppink")
  case object DeepskyBlue           extends NamedColor("deepskyblue")
  case object DimGray               extends NamedColor("dimgray")
  case object DimGrey               extends NamedColor("dimgrey")
  case object DodgerBlue            extends NamedColor("dodgerblue")
  case object Firebrick             extends NamedColor("firebrick")
  case object FloralWhite           extends NamedColor("floralwhite")
  case object ForestGreen           extends NamedColor("forestgreen")
  case object Fuchsia               extends NamedColor("fuchsia")
  case object Gainsboro             extends NamedColor("gainsboro")
  case object Ghostwhite            extends NamedColor("ghostwhite")
  case object Gold                  extends NamedColor("gold")
  case object Goldenrod             extends NamedColor("goldenrod")
  case object Gray                  extends NamedColor("gray")
  case object Green                 extends NamedColor("green")
  case object GreenYellow           extends NamedColor("greenyellow")
  case object Grey                  extends NamedColor("grey")
  case object Honeydew              extends NamedColor("honeydew")
  case object HotPink               extends NamedColor("hotpink")
  case object IndianRed             extends NamedColor("indianred")
  case object Indigo                extends NamedColor("indigo")
  case object Ivory                 extends NamedColor("ivory")
  case object Khaki                 extends NamedColor("khaki")
  case object Lavender              extends NamedColor("lavender")
  case object LavenderBlush         extends NamedColor("lavenderblush")
  case object LawnGreen             extends NamedColor("lawngreen")
  case object LemonChiffon          extends NamedColor("lemonchiffon")
  case object LightBlue             extends NamedColor("lightblue")
  case object LightCoral            extends NamedColor("lightcoral")
  case object Lightyan              extends NamedColor("lightcyan")
  case object LightGoldenrodYellow  extends NamedColor("lightgoldenrodyellow")
  case object LightGray             extends NamedColor("lightgray")
  case object LightGreen            extends NamedColor("lightgreen")
  case object LightGrey             extends NamedColor("lightgrey")
  case object LightPink             extends NamedColor("lightpink")
  case object LightSalmon           extends NamedColor("lightsalmon")
  case object LightSeaGreen         extends NamedColor("lightseagreen")
  case object LightSkyBlue          extends NamedColor("lightskyblue")
  case object LightSlateGray        extends NamedColor("lightslategray")
  case object LightSlateGrey        extends NamedColor("lightslategrey")
  case object LightSteelblue        extends NamedColor("lightsteelblue")
  case object LightYellow           extends NamedColor("lightyellow")
  case object Lime                  extends NamedColor("lime")
  case object LimeGreen             extends NamedColor("limegreen")
  case object Linen                 extends NamedColor("linen")
  case object Magenta               extends NamedColor("magenta")
  case object Maroon                extends NamedColor("maroon")
  case object MediumAquamarine      extends NamedColor("mediumaquamarine")
  case object MediumBlue            extends NamedColor("mediumblue")
  case object MediumOrchid          extends NamedColor("mediumorchid")
  case object MediumPurple          extends NamedColor("mediumpurple")
  case object MediumSeagreen        extends NamedColor("mediumseagreen")
  case object MediumSlateBlue       extends NamedColor("mediumslateblue")
  case object MediumSpringGreen     extends NamedColor("mediumspringgreen")
  case object MediumTurquoise       extends NamedColor("mediumturquoise")
  case object MediumVioletRed       extends NamedColor("mediumvioletred")
  case object MidnightBlue          extends NamedColor("midnightblue")
  case object MintCream             extends NamedColor("mintcream")
  case object MistyRose             extends NamedColor("mistyrose")
  case object Moccasin              extends NamedColor("moccasin")
  case object NavajoWhite           extends NamedColor("navajowhite")
  case object Navy                  extends NamedColor("navy")
  case object Oldlace               extends NamedColor("oldlace")
  case object Olive                 extends NamedColor("olive")
  case object Olivedrab             extends NamedColor("olivedrab")
  case object Orange                extends NamedColor("orange")
  case object OrangeRed             extends NamedColor("orangered")
  case object Orchid                extends NamedColor("orchid")
  case object PaleGoldenrod         extends NamedColor("palegoldenrod")
  case object PaleGreen             extends NamedColor("palegreen")
  case object PaleTurquoise         extends NamedColor("paleturquoise")
  case object PaleVioletRed         extends NamedColor("palevioletred")
  case object Papayawhip            extends NamedColor("papayawhip")
  case object PeachPuff             extends NamedColor("peachpuff")
  case object Peru                  extends NamedColor("peru")
  case object Pink                  extends NamedColor("pink")
  case object Plum                  extends NamedColor("plum")
  case object PowderBlue            extends NamedColor("powderblue")
  case object Purple                extends NamedColor("purple")
  case object Red                   extends NamedColor("red")
  case object RosyBrown             extends NamedColor("rosybrown")
  case object RoyalBlue             extends NamedColor("royalblue")
  case object SaddleBrown           extends NamedColor("saddlebrown")
  case object Salmon                extends NamedColor("salmon")
  case object SandyBrown            extends NamedColor("sandybrown")
  case object Seagreen              extends NamedColor("seagreen")
  case object Seashell              extends NamedColor("seashell")
  case object Sienna                extends NamedColor("sienna")
  case object Silver                extends NamedColor("silver")
  case object SkyBlue               extends NamedColor("skyblue")
  case object SlateBlue             extends NamedColor("slateblue")
  case object SlateGray             extends NamedColor("slategray")
  case object SlateGrey             extends NamedColor("slategrey")
  case object Snow                  extends NamedColor("snow")
  case object SpringGreen           extends NamedColor("springgreen")
  case object SteelBlue             extends NamedColor("steelblue")
  case object Tan                   extends NamedColor("tan")
  case object Teal                  extends NamedColor("teal")
  case object Thistle               extends NamedColor("thistle")
  case object Tomato                extends NamedColor("tomato")
  case object Turquoise             extends NamedColor("turquoise")
  case object Violet                extends NamedColor("violet")
  case object Wheat                 extends NamedColor("wheat")
  case object White                 extends NamedColor("white")
  case object WhiteSmoke            extends NamedColor("whitesmoke")
  case object Yellow                extends NamedColor("yellow")
  case object YellowGreen           extends NamedColor("yellowgreen")
}

case class Extent(width: Double, height: Double)
trait Renderable {
  // bounding boxen must be of stable size
  val extent: Extent
  def render(canvas: CanvasRenderingContext2D): Unit
}

case class Style(fill: Colors.Color)(r: Renderable) extends Renderable {
  val extent = r.extent
  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas){ c =>
      c.fillStyle = fill.repr
      r.render(c)
    }
}

case class Line(length: Double, strokeWidth: Double) extends Renderable {

  val extent = Extent(length, strokeWidth)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.beginPath()
      canvas.lineWidth = strokeWidth
      canvas.moveTo(0, strokeWidth / 2.0)
      canvas.lineTo(length, strokeWidth / 2.0)
      canvas.closePath()
      canvas.stroke()
    }
}

case class Rect(width: Double, height: Double) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit = canvas.fillRect(0, 0, width, height)
  val extent: Extent = Extent(width, height)
}
object Rect {
  def apply(side: Double): Rect = Rect(side, side)
  def apply(size: Extent): Rect = Rect(size.width, size.height)
}

case class Disc(radius: Double, x: Double = 0, y: Double = 0) extends Renderable {
  require(x >= 0 && y >=0, s"x {$x} and y {$y} must both be positive")
  val extent = Extent(x + radius * 2, y + radius * 2)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.beginPath()
      c.arc(x + radius, y + radius, radius, 0, 2 * Math.PI)
      c.closePath()
      c.fill()
    }
}

case class Wedge(angleDegrees: Double, radius: Double) extends Renderable {
  val extent = Extent(2 * radius, 2 * radius)

  def render(canvas: CanvasRenderingContext2D): Unit = {
    CanvasOp(canvas) { c =>
      c.translate(radius, radius)
      c.beginPath()
      c.moveTo(0, 0)
      c.arc(0, 0, radius, -Math.PI * angleDegrees / 360.0, Math.PI * angleDegrees / 360.0)
      c.closePath()
      c.fill()
    }
  }
}

case class Text(msgAny: Any, size: Double = Text.defaultSize) extends Renderable {
  require(size >= 0.5, s"Cannot use ${size}, canvas will not render text initially sized < 0.5px even when scaling")
  private val msg = msgAny.toString

  val extent: Extent = Text.measure(size)(msg)

  def render(canvas: CanvasRenderingContext2D): Unit = Text.withStyle(size){_.fillText(msg, 0, 0)}(canvas)
}
object Text {
  val defaultSize = 10

  // TODO: THIS IS A DIRTY HACK
  private val offscreenBuffer = dom.window.document.getElementById("measureBuffer").asInstanceOf[html.Canvas].getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val replaceSize = """\d+px""".r
  // TODO: Text this regex esp on 1px 1.0px 1.px .1px, what is valid in CSS?
  private val fontSize = """[^\d]*([\d(?:\.\d*)]+)px.*""".r
  private def extractHeight = {
    val fontSize(size) = offscreenBuffer.font
    size.toDouble
  }

  private def swapFont(canvas: CanvasRenderingContext2D, size: Double) = {
    Text.replaceSize.replaceFirstIn(canvas.font, size.toString + "px")
  }

  private def withStyle[T](size: Double)(f: CanvasRenderingContext2D => T): CanvasRenderingContext2D => T = {
    c =>
      c.textBaseline = "top"
      c.font = swapFont(c, size)
      f(c)
  }

  private def measure(size: Double)(msg: String) = withStyle(size){ c =>
    Extent(c.measureText(msg).width, extractHeight)
  }(offscreenBuffer)
}

object CanvasOp {
  // loan it out
  def apply(canvas: CanvasRenderingContext2D)(f: CanvasRenderingContext2D => Unit) = {
    canvas.save()
    f(canvas)
    canvas.restore()
  }
}

case class Scale(x: Double = 1, y: Double = 1)(r: Renderable) extends Renderable {
  val extent = Extent( r.extent.width * y, r.extent.height * x )

  def render(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas){ c =>
    c.scale(x, y)
    r.render(c)
  }
}

case class FlipY(r: Renderable) extends Renderable {
  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    Translate(y = r.extent.height){
      Scale(1, -1)(r)
    }.render(canvas)
}

case class FlipX(r: Renderable) extends Renderable {
  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    Translate(x = r.extent.width){
      Scale(-1, 1)(r)
    }.render(canvas)
}

case class Translate(x: Double = 0, y: Double = 0)(r: Renderable) extends Renderable {
  // TODO: is this correct with negative translations?
  val extent: Extent = Extent(
    r.extent.width + x,
    r.extent.height + y
  )

  def render(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas){ c =>
    c.translate(x, y)
    r.render(c)
  }
}
object Translate {
  def apply(r: Renderable, bbox: Extent): Translate = Translate(bbox.width, bbox.height)(r)
}

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
case class Rotate(degrees: Double)(r: Renderable) extends Renderable {

  // TODO: not bringing in a matrix library for just this one thing ... yet
  private case class Point(x: Double, y: Double) {
    def originRotate(thetaDegrees: Double): Point = {
      val thetaRad = math.toRadians(thetaDegrees)
      Point(
        x * math.cos(thetaRad) - y * math.sin(thetaRad),
        y * math.cos(thetaRad) + x * math.sin(thetaRad)
      )
    }
  }

  private val rotatedCorners = Seq(
    Point(-0.5 * r.extent.width, -0.5 * r.extent.height),
    Point( 0.5 * r.extent.width, -0.5 * r.extent.height),
    Point(-0.5 * r.extent.width,  0.5 * r.extent.height),
    Point( 0.5 * r.extent.width,  0.5 * r.extent.height)
  ).map(_.originRotate(degrees))

  private val minX = rotatedCorners.map(_.x).min
  private val maxX = rotatedCorners.map(_.x).max
  private val minY = rotatedCorners.map(_.y).min
  private val maxY = rotatedCorners.map(_.y).max

  val extent = Extent(maxX - minX, maxY - minY)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(-1 * minX , -1 * minY)
      c.rotate(math.toRadians(degrees))
      c.translate(r.extent.width / -2, r.extent.height / -2)

      r.render(c)
    }
}

//TODO: A future way to eliminate this is:
// * replace "extents" and reaching into the object with a more sophisticated class
// * that class should support widestWidth, tallestHeight, and a rotate method that returns a new copy with same wW/tH
// * then extents can be arbitrary polygons instead of just Rect's
// end TODO

// Our rotate semantics are, rotate about your centroid, and shift back to all positive coordinates
// BUT CircularExtented things' rotated extents cannot be computed as a rotated rectangles, they are assumed invariant
case class UnsafeRotate(degrees: Double)(r: Renderable) extends Renderable {

  val extent = r.extent

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(extent.width / 2, extent.height / 2)
      c.rotate(math.toRadians(degrees))
      c.translate(extent.width / -2, extent.height / -2)

      r.render(c)
    }
}

case class Pad(left: Double = 0, right: Double = 0, top: Double = 0, bottom: Double = 0)(item: Renderable) extends Renderable {
  val extent = Extent(
    item.extent.width + left + right,
    item.extent.height + top + bottom
  )

  def render(canvas: CanvasRenderingContext2D): Unit = Translate(x = left, y = top)(item).render(canvas)
}
object Pad {
  def apply(surround: Double)(item: Renderable): Pad = Pad(surround, surround, surround, surround)(item)
  def apply(x: Double, y: Double)(item: Renderable): Pad = Pad(x, x, y, y)(item)
}

case class Group(items: Renderable*) extends Renderable {
  val extent: Extent = Extent(
    items.map(_.extent.width).max,
    items.map(_.extent.height).max
  )

  def render(canvas: CanvasRenderingContext2D): Unit = items.foreach(_.render(canvas))
}

case class Above(top: Renderable, bottom: Renderable) extends Renderable {
  val extent: Extent = Extent(
    math.max(top.extent.width, bottom.extent.width),
    top.extent.height + bottom.extent.height
  )

  def render(canvas: CanvasRenderingContext2D): Unit = (
      top behind Translate(y = top.extent.height)(bottom)
    ).render(canvas)
}

case class Beside(head: Renderable, tail: Renderable) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit =
    (
      head behind Translate(x = head.extent.width)(tail)
    ).render(canvas)

  val extent: Extent = Extent(
    head.extent.width + tail.extent.width,
    math.max(head.extent.height, tail.extent.height)
  )
}


object Align {
  def bottomSeq(items: Seq[Renderable]) = bottom(items :_*)

  def bottom(items: Renderable*): Seq[Renderable] = {
    val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map(r => Translate(y = groupHeight - r.extent.height)(r) )
  }

  def centerSeq(items: Seq[Renderable]) = center(items :_*)

  def center(items: Renderable*): Seq[Renderable] = {
    val groupWidth = items.maxBy(_.extent.width).extent.width

    items.map( r => Translate(x = (groupWidth - r.extent.width) / 2.0)(r) )
  }

  def middle(items: Renderable*): Seq[Renderable] = {
    val groupHeight = items.maxBy(_.extent.height).extent.height

    items.map( r => Translate(y = (groupHeight - r.extent.height) / 2.0)(r) )
  }
}

case class Labeled(msg: String, r: Renderable, textSize: Double = Text.defaultSize) extends Renderable {

  private val composite = Align.center(r, Text(msg, textSize) padTop 5 ).reduce(Above)

  val extent: Extent = composite.extent
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

case class Titled(msg: String, r: Renderable, textSize: Double = Text.defaultSize) extends Renderable {

  private val paddedTitle = Pad(bottom = textSize / 2.0)(Text(msg, textSize))
  private val composite = Align.center(paddedTitle, r).reduce(Above)

  val extent = composite.extent
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

case class Fit(width: Double, height: Double)(item: Renderable) extends Renderable {
  val extent = Extent(width, height)

  def render(canvas: CanvasRenderingContext2D): Unit = {
    val oldExtent = item.extent

    val newAspectRatio = width / height
    val oldAspectRatio = oldExtent.width / oldExtent.height

    val widthIsLimiting = newAspectRatio < oldAspectRatio

    val (scale, padFun) = if(widthIsLimiting) {
      val scale = width / oldExtent.width
      (
        scale,
        Pad(top = ((height - oldExtent.height * scale) / 2) / scale) _
      )
    } else { // height is limiting
      val scale = height / oldExtent.height
      (
        scale,
        Pad(left = ((width - oldExtent.width * scale) / 2) / scale) _
      )
    }

    CanvasOp(canvas){c =>
      c.scale(scale, scale)
      padFun(item).render(c)
    }
  }
}
object Fit {
  def apply(extent: Extent)(item: Renderable): Fit = Fit(extent.width, extent.height)(item)
}

object DSL {
  implicit class Placeable(r: Renderable){
    def above(other: Renderable) = Above(r, other)
    def below(other: Renderable) = Above(other, r)
    def beside(other: Renderable) = Beside(r, other)
    def behind(other: Renderable) = Group(r, other)

    def labeled(msg: String) = Labeled(msg, r)
    def labeled(msgSize: (String, Double)) = Labeled(msgSize._1, r, msgSize._2)
    def titled(msg: String) = Titled(msg, r)
    def titled(msgSize: (String, Double)) = Titled(msgSize._1, r, msgSize._2)

    def padRight(pad: Double) = Pad(right = pad)(r)
    def padLeft(pad: Double) = Pad(left  = pad)(r)
    def padBottom(pad: Double) = Pad(bottom = pad)(r)
    def padTop(pad: Double) = Pad(top = pad)(r)
    def padAll(pad: Double) = Pad(pad)(r)

    def rotated(degress: Double) = Rotate(degress)(r)

    def filled(color: Colors.Color) = Style(fill = color)(r)

    // Experimental
    def -->(nudge: Double) = Translate(x = nudge)(r)
    def |^(nudge: Double) = Translate(y = nudge)(r)
    def -|(pad: Double) = Pad(right = pad)(r)
    def |-(pad: Double) = Pad(left  = pad)(r)
    def ⊥(pad: Double) = Pad(bottom = pad)(r)
    def ⊤(pad: Double) = Pad(top = pad)(r)

    // end Experimental
  }

  implicit class SeqPlaceable(sp: Seq[Renderable]){
    def distributeH: Renderable = DistributeH(sp)
    def distributeH(spacing: Double = 0): Renderable = DistributeH(sp, spacing)
    def distributeV: Renderable = DistributeV(sp)

    def group: Renderable = Group(sp :_*)
  }


  def FlowH(rs: Seq[Renderable], hasWidth: Extent): Renderable = {
    val consumed = rs.map(_.extent.width).sum
    val inBetween = (hasWidth.width - consumed) / (rs.length - 1)
    val padded = rs.init.map(_ padRight inBetween) :+ rs.last
    padded.reduce(Beside)
  }

  def DistributeH(rs: Seq[Renderable], spacing: Double = 0): Renderable =
    if(spacing == 0) rs.reduce(Beside)
    else {
      val padded = rs.init.map(_ padRight spacing) :+ rs.last
      padded.reduce(Beside)
    }

  def DistributeV(rs: Seq[Renderable]): Renderable = rs.reduce(Above)
}