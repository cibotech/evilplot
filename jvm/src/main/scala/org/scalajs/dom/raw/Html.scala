package org.scalajs.dom.raw

abstract class HTMLDocument {
  def getElementById(elementId: String): HTMLCanvasElement = ???
}

class Window {
  def document: HTMLDocument = ???
  def innerWidth: Double = ???
  def innerHeight: Double = ???
}

abstract class CSSStyleDeclaration {
  var width: String = ???
  var height: String = ???
}
// scalastyle:off not.implemented.error.usage
abstract class HTMLCanvasElement {
  var width: Int = ???

  var height: Int = ???

  var style: CSSStyleDeclaration = ???

  def toDataURL(`type`: String, args: Any*): String = ???

  def getContext(contextId: String, args: Any*): CanvasRenderingContext2D = ???
}

class CanvasRenderingContext2D {

  var miterLimit: Double = ???

  var font: String = ???

  var globalCompositeOperation: String = ???

  var lineCap: String = ???

  var lineDashOffset: Double = ???

  var shadowColor: String = ???

  var lineJoin: String = ???

  var shadowOffsetX: Double = ???

  var lineWidth: Double = ???

  var canvas: HTMLCanvasElement = ???

  var strokeStyle: Any = ???

  var globalAlpha: Double = ???

  var shadowOffsetY: Double = ???

  var fillStyle: Any = ???

  var shadowBlur: Double = ???

  var textAlign: String = ???

  var textBaseline: String = ???

  def restore(): Unit = ???

  def setTransform(m11: Double, m12: Double, m21: Double, m22: Double,
                   dx: Double, dy: Double): Unit = ???

  def save(): Unit = ???

  def arc(x: Double, y: Double, radius: Double, startAngle: Double,
          endAngle: Double, anticlockwise: Boolean): Unit = ???

  def arc(x: Double, y: Double, radius: Double, startAngle: Double,
          endAngle: Double): Unit = ???

  def measureText(text: String): TextMetrics = ???

  def isPointInPath(x: Double, y: Double,
                    fillRule: String): Boolean = ???

  def isPointInPath(x: Double, y: Double): Boolean = ???

  def quadraticCurveTo(cpx: Double, cpy: Double, x: Double,
                       y: Double): Unit = ???


  def rotate(angle: Double): Unit = ???

  // This is the real signature.
/*  def fillText(text: String, x: Double, y: Double,
               maxWidth: Double): Unit = ???*/
  // We use this for now.
  def fillText(text: String, x: Double, y: Double): Unit = ???

  def translate(x: Double, y: Double): Unit = ???

  def scale(x: Double, y: Double): Unit = ???

  def lineTo(x: Double, y: Double): Unit = ???

  def fill(): Unit = ???

  def closePath(): Unit = ???

  def rect(x: Double, y: Double, w: Double, h: Double): Unit = ???


  def clearRect(x: Double, y: Double, w: Double, h: Double): Unit = ???

  def moveTo(x: Double, y: Double): Unit = ???

  def fillRect(x: Double, y: Double, w: Double, h: Double): Unit = ???

  def bezierCurveTo(cp1x: Double, cp1y: Double, cp2x: Double, cp2y: Double,
                    x: Double, y: Double): Unit = ???

  def transform(m11: Double, m12: Double, m21: Double, m22: Double, dx: Double,
                dy: Double): Unit = ???

  def stroke(): Unit = ???

  def strokeRect(x: Double, y: Double, w: Double, h: Double): Unit = ???


  def strokeText(text: String, x: Double, y: Double,
                 maxWidth: Double): Unit = ???

  def beginPath(): Unit = ???

  def arcTo(x1: Double, y1: Double, x2: Double, y2: Double,
            radius: Double): Unit = ???

}
// scalastyle:on not.implemented.error.usage

