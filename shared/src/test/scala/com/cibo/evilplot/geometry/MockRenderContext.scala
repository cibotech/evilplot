package com.cibo.evilplot.geometry

class MockRenderContext extends RenderContext {
  def draw(line: Line): Unit = ???
  def draw(path: Path): Unit = ???
  def draw(rect: Rect): Unit = ???
  def draw(rect: BorderRect): Unit = ???
  def draw(disc: Disc): Unit = ???
  def draw(wedge: Wedge): Unit = ???
  def draw(translate: Translate): Unit = ???
  def draw(affine: Affine): Unit = ???
  def draw(scale: Scale): Unit = ???
  def draw(rotate: Rotate): Unit = ???
  def draw(rotate: UnsafeRotate): Unit = ???
  def draw(style: Style): Unit = ???
  def draw(style: StrokeStyle): Unit = ???
  def draw(weight: StrokeWeight): Unit = ???
  def draw(text: Text): Unit = ???
}
