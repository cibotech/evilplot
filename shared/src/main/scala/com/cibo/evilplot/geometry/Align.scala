package com.cibo.evilplot.geometry

object Align {
  def bottomSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height
    items.map(r => Translate(r, y = groupHeight - r.extent.height))
  }
  def bottom(items: Drawable*): Seq[Drawable] = bottomSeq(items)

  def centerSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width
    items.map(r => Translate(r, x = (groupWidth - r.extent.width) / 2.0))
  }
  def center(items: Drawable*): Seq[Drawable] = centerSeq(items)

  def rightSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupWidth = items.maxBy(_.extent.width).extent.width
    items.map(r => Translate(r, x = groupWidth - r.extent.width))
  }
  def right(items: Drawable*): Seq[Drawable] = rightSeq(items)

  def middleSeq(items: Seq[Drawable]): Seq[Drawable] = {
    lazy val groupHeight = items.maxBy(_.extent.height).extent.height
    items.map(r => Translate(r, y = (groupHeight - r.extent.height) / 2.0))
  }
  def middle(items: Drawable*): Seq[Drawable] = middleSeq(items)
}

