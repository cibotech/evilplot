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

package com.cibo.evilplot.geometry

object Align {
  def bottomSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height
    items.map(r => r.translate(y = groupHeight - r.extent.height))
  }
  def bottom(items: Drawable*): Seq[Drawable] = bottomSeq(items)

  def centerSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width
    items.map(r => r.translate(x = (groupWidth - r.extent.width) / 2.0))
  }
  def center(items: Drawable*): Seq[Drawable] = centerSeq(items)

  def rightSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width
    items.map(r => r.translate(x = groupWidth - r.extent.width))
  }
  def right(items: Drawable*): Seq[Drawable] = rightSeq(items)

  def middleSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height
    items.map(r => r.translate(y = (groupHeight - r.extent.height) / 2.0))
  }
  def middle(items: Drawable*): Seq[Drawable] = middleSeq(items)
}
