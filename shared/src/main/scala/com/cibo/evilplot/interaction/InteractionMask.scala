/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
