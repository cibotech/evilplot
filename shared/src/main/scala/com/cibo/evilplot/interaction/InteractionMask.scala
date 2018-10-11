package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry.InteractionEvent

import scala.annotation.tailrec
import scala.util.{Failure, Random, Success, Try}

/*
  This provides interaction tracking using a color mask.
  It should provide tracking for up to millions of element, Limited by colors in RGB space and likelyhood of collision.
  However, you'll likely be limited by memory first.
  This works well for use cases where the interaction mask is rendered less often than the main drawable.
*/
trait InteractionMask {

  val noInteraction: String = s"#FFFFFF"

  protected def getImageData(x: Double, y: Double): Array[Int]

  private var eventListeners: Map[Int, Seq[InteractionEvent]] = Map()

  protected def addEvents(event: Seq[InteractionEvent]) = {
    val key = nextIndexValue
    eventListeners = eventListeners + (key -> event)

    key.toHexString.reverse.padTo(6, "0").reverse.mkString
  }

  def clearEventListeners(): Unit = eventListeners = Map()

  // key generation takes progressively longer as its a random search (this can probably be improved)
  @tailrec
  protected final def nextIndexValue: Int = {

    val next = Random.nextInt(16777216)
    if(eventListeners.keys.exists(_ == next)){
      nextIndexValue
    } else next
  }

  def events(x: Double, y: Double): Seq[InteractionEvent] = {

    val pixelData = getImageData(x, y)
    if(pixelData(3) == 255) { // only match non-alpha colors

      // calcuate idx from rgb
      val idx = (((pixelData(0) * 256 * 256) + (pixelData(1) * 256) + pixelData(2)))

      eventListeners.get(idx).toSeq.flatten
    } else Seq()
  }

}
