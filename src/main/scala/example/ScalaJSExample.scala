package example
import scala.scalajs.js.annotation.JSExport
import org.scalajs.dom
import org.scalajs.dom.{CanvasRenderingContext2D, html}

import DSL._

case class Point(x: Int, y: Int){
  def +(p: Point) = Point(x + p.x, y + p.y)
  def /(d: Int) = Point(x / d, y / d)
}

@JSExport
object ScalaJSExample {

  @JSExport
  def main(canvas: html.Canvas): Unit = {
    val ctx: CanvasRenderingContext2D = canvas.getContext("2d")
                    .asInstanceOf[dom.CanvasRenderingContext2D]

    ctx.canvas.width = dom.window.innerWidth.toInt
    ctx.canvas.height = dom.window.innerHeight.toInt

    val scale = 3

    val heights = Seq(10, 100, 200).map(_ * scale)

    val colors = Seq("red", "green", "blue")

    val textAndPadHeight = 20 + 5 + 0.25 // text size, label pad, stroke width

    val tickThick = 0.25
    val tickWidth = 10 * scale
    val yAxis = Pad(top = textAndPadHeight - tickWidth){ // + tickWidth for the extra last tick
      val numTicks = heights.max / tickWidth
      val ticks = DistributeH(
        Seq.fill(numTicks + 1)(
          Pad(right = tickWidth - tickThick)(Rotate(90)(Line(5 * scale, tickThick)))
        )
      )

      Rotate(-90)(
        Group(
          Line(heights.max, 2),
          ticks
        ) titled "Awesomeness")
    }

    val barWidth = 50 * scale
    val barSpacing = 5

    val bars = DistributeH(
      Align.bottom {
        val rects = heights.map(h => Pad(right = barSpacing)(Rect(barWidth, h) titled (h / scale).toString))
        rects.zip(colors).map { case (rect, color) => Style(color)(rect) labeled color }
      }
    )

    val textHeight = 20

    val gridLines = DistributeV {
      val lineEveryXUnits = 40 * scale
      val lineThick = 0.25
      val textHalfHeight = textHeight / 2

      Seq.tabulate(heights.max / lineEveryXUnits){ x =>
        val yHeightLabel = (heights.max / lineEveryXUnits - x) * lineEveryXUnits / scale
        Pad(bottom = lineEveryXUnits - lineThick){
          val label = Translate(y = -textHeight)(Style("grey")(Text(yHeightLabel.toString)))
          Group(
            Line(bars.boundingBox.width, lineThick),
            label
          )
        }
      }
    }

    val barChart =
      yAxis beside
      Group(
        Pad(top = textAndPadHeight)(gridLines),
        bars
      )


    Pad(10){
      DistributeV(
        Align.center(Seq(
          Pad(10)(Text("A Swanky BarChart", size = 20 * scale)),
          barChart
        ))
      )
    }.render(ctx)
  }
}


case class BoundingBox(width: Double, height: Double)
trait Renderable {
  // bounding boxen must be of stable size
  val boundingBox: BoundingBox
  def render(canvas: CanvasRenderingContext2D): Unit
}

case class Style(color: String)(r: Renderable) extends Renderable {
  val boundingBox = r.boundingBox
  def render(canvas: CanvasRenderingContext2D): Unit = {
    CanvasOp(canvas){ c =>
      c.fillStyle = color
      r.render(c)
    }
  }
}

case class Line(length: Double, strokeWidth: Double) extends Renderable {

  val boundingBox = BoundingBox(length, strokeWidth)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      canvas.lineWidth = strokeWidth
      canvas.moveTo(0, strokeWidth / 2.0)
      canvas.lineTo(length, strokeWidth / 2.0)
      canvas.stroke()
    }
}

case class Rect(width: Double, height: Double) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit = canvas.fillRect(0, 0, width, height)
  val boundingBox: BoundingBox = BoundingBox(width, height)
}
object Rect {
  def apply(side: Double): Rect = Rect(side, side)
}

case class Text(msg: String, size: Double = 20) extends Renderable {
  val boundingBox: BoundingBox = Text.measure(size)(msg)

  def render(canvas: CanvasRenderingContext2D): Unit = Text.withStyle(size){_.fillText(msg, 0, 0)}(canvas)
}
object Text {
  // TODO: THIS IS A DIRTY HACK
  private val offscreenBuffer = dom.window.document.getElementById("measureBuffer").asInstanceOf[html.Canvas].getContext("2d").asInstanceOf[dom.CanvasRenderingContext2D]

  private val replaceSize = """\d+px""".r
  private val fontSize = """[^\d]*(\d+)px.*""".r
  private def extractHeight = {
    val fontSize(size) = offscreenBuffer.font
    size.toDouble
  }

  private def swapFont(canvas: CanvasRenderingContext2D, size: Double) =
    Text.replaceSize.replaceFirstIn(canvas.font, size.toString + "px")

  private def withStyle[T](size: Double)(f: CanvasRenderingContext2D => T): CanvasRenderingContext2D => T = {
    c =>
      c.textBaseline = "top"
      c.font = swapFont(c, size)
      f(c)
  }

  private def measure(size: Double)(msg: String) = withStyle(size){ c =>
    BoundingBox(c.measureText(msg).width, extractHeight)
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
case class Translate(x: Double = 0, y: Double = 0)(r: Renderable) extends Renderable {
  // TODO: is this correct with negative translations?
  val boundingBox: BoundingBox = BoundingBox(
    r.boundingBox.width + x,
    r.boundingBox.height + y
  )

  def render(canvas: CanvasRenderingContext2D): Unit = CanvasOp(canvas){ c =>
    c.translate(x, y)
    r.render(c)
  }
}
object Translate {
  def apply(r: Renderable, bbox: BoundingBox): Translate = Translate(bbox.width, bbox.height)(r)
}

// Our rotate sematics are, rotate about your centroid, and shift back to all positive coordinates
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
    Point(-0.5 * r.boundingBox.width, -0.5 * r.boundingBox.height),
    Point( 0.5 * r.boundingBox.width, -0.5 * r.boundingBox.height),
    Point(-0.5 * r.boundingBox.width,  0.5 * r.boundingBox.height),
    Point( 0.5 * r.boundingBox.width,  0.5 * r.boundingBox.height)
  ).map(_.originRotate(degrees))

  private val minX = rotatedCorners.map(_.x).min
  private val maxX = rotatedCorners.map(_.x).max
  private val minY = rotatedCorners.map(_.y).min
  private val maxY = rotatedCorners.map(_.y).max

  val boundingBox = BoundingBox(maxX - minX, maxY - minY)

  def render(canvas: CanvasRenderingContext2D): Unit =
    CanvasOp(canvas) { c =>
      c.translate(-1 * minX , -1 * minY)
      c.rotate(math.toRadians(degrees))
      c.translate(r.boundingBox.width / -2, r.boundingBox.height / -2)

      r.render(c)
    }
}

case class Pad(left: Double = 0, right: Double = 0, top: Double = 0, bottom: Double = 0)(item: Renderable) extends Renderable {
  val boundingBox = BoundingBox(
    item.boundingBox.width + left + right,
    item.boundingBox.height + top + bottom
  )

  def render(canvas: CanvasRenderingContext2D): Unit = Translate(x = left, y = top)(item).render(canvas)
}
object Pad {
  def apply(surround: Double)(item: Renderable): Pad = Pad(surround, surround, surround, surround)(item)
  def apply(x: Double, y: Double)(item: Renderable): Pad = Pad(x, x, y, y)(item)
}

case class Group(items: Renderable*) extends Renderable {
  val boundingBox: BoundingBox = BoundingBox(
    items.map(_.boundingBox.width).max,
    items.map(_.boundingBox.height).max
  )

  def render(canvas: CanvasRenderingContext2D): Unit = items.foreach(_.render(canvas))
}

case class Above(top: Renderable, bottom: Renderable) extends Renderable {
  val boundingBox: BoundingBox = BoundingBox(
    math.max(top.boundingBox.width, bottom.boundingBox.width),
    top.boundingBox.height + bottom.boundingBox.height
  )

  def render(canvas: CanvasRenderingContext2D): Unit =
    Group(
      top,
      Translate(y = top.boundingBox.height)(bottom)
    ).render(canvas)
}

case class Beside(head: Renderable, tail: Renderable) extends Renderable {
  def render(canvas: CanvasRenderingContext2D): Unit =
    Group(
      head,
      Translate(x = head.boundingBox.width)(tail)
    ).render(canvas)

  val boundingBox: BoundingBox = BoundingBox(
    head.boundingBox.width + tail.boundingBox.width,
    math.max(head.boundingBox.height, tail.boundingBox.height)
  )
}


object Align {
  def bottom(items: Seq[Renderable]): Seq[Renderable] = {
    val groupHeight = items.foldLeft(0D)((max, elem) => math.max(max, elem.boundingBox.height))

    items.map(r => Translate(y = groupHeight - r.boundingBox.height)(r) )
  }

  def center(items: Seq[Renderable]): Seq[Renderable] = {
    val groupWidth = items.foldLeft(0D)((max, elem) => math.max(max, elem.boundingBox.width))

    items.map( r => Translate(x = (groupWidth - r.boundingBox.width) / 2.0)(r) )
  }
}

case class Labeled(msg: String, r: Renderable) extends Renderable {

  private val composite = Align.center(Seq(r, Pad(top = 5)(Text(msg)))).reduce(Above)

  val boundingBox: BoundingBox = composite.boundingBox
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

case class Titled(msg: String, r: Renderable) extends Renderable {

  private val composite = Align.center(Seq( Pad(bottom = 5)(Text(msg)), r)).reduce(Above)

  val boundingBox: BoundingBox = composite.boundingBox
  def render(canvas: CanvasRenderingContext2D): Unit = composite.render(canvas)
}

object DSL {
  implicit class Placeable(r: Renderable){
    def above(other: Renderable) = Above(r, other)
    def beside(other: Renderable) = Beside(r, other)
    def labeled(msg: String) = Labeled(msg, r)
    def titled(msg: String) = Titled(msg, r)
  }

  def DistributeH(rs: Seq[Renderable]): Renderable = rs.reduce(Beside)
  def DistributeV(rs: Seq[Renderable]): Renderable = rs.reduce(Above)
}