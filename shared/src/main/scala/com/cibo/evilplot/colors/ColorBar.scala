package com.cibo.evilplot.colors

import com.cibo.evilplot.numeric.AxisDescriptor
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

sealed trait ColorBar {
  val nColors: Int
  def getColor(z: Int): Color
}

// Use when one color is wanted but a ColorBar is needed.
case class SingletonColorBar(color: Color) extends ColorBar {
  val nColors: Int = 1
  def getColor(z: Int): Color = {
    require(z == 1)
    color
  }
}

// Map a sequence of colors to a continuous variable z.
case class ScaledColorBar(colorSeq: Seq[Color], zMin: Double, zMax: Double) extends ColorBar {
  val nColors: Int = colorSeq.length

  private val zWidth = (zMax - zMin) / nColors.toFloat

  def getColor(i: Int): Color = colorSeq(i)
  def getColor(z: Double): Color = getColor(colorIndex(z))

  def colorIndex(z: Double): Int = math.min(math.round(math.floor((z - zMin) / zWidth)).toInt, nColors - 1)
  def colorValue(i: Int): Double = i * zWidth + zMin
}

object ColorBar {
  implicit val encoder: Encoder[ColorBar] = deriveEncoder[ColorBar]
  implicit val decoder: Decoder[ColorBar] = deriveDecoder[ColorBar]
}

