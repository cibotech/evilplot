package com.cibo.evilplot.layout

import com.cibo.evilplot.geometry.{Drawable, DrawableLater, EmptyDrawable, EmptyDrawableLater, Extent}
import org.scalajs.dom.raw.CanvasRenderingContext2D

/** LayoutManagers take DrawableLaters, objects which take an extent and return a Drawable.
 * The LayoutManagers determine the extents of the rendered drawables and place them appropriately.
 */

trait LayoutManager extends Drawable {
}

/** ChartLayout creates a `typical` chart, allowing one to position a chart area
  * at center with other Drawable objects surrounding, as below:
  * XXX| TOP |XXX
  * -------------
  * L  | CEN |
  * E  | TER | RIGHT
  * FT |     |
  * -------------
  *XXX | BOT |XXX
  *    | TOM |
  */
// TODO: Preferred size on any of the components of the layout with some sort of hierarchy to decide which to go with.
case class ChartLayout(extent: Extent,
                       preferredSizeOfCenter: Extent,
                       center: DrawableLater = EmptyDrawableLater,
                       top: DrawableLater = EmptyDrawableLater,
                       bottom: DrawableLater = EmptyDrawableLater,
                       left: DrawableLater = EmptyDrawableLater,
                       right: DrawableLater = EmptyDrawableLater) extends LayoutManager {
  require(preferredSizeOfCenter.width <= extent.width, preferredSizeOfCenter.height < extent.height)

  private def createParallelBorderDrawables(a: DrawableLater, b: DrawableLater,
                                            extentIfOne: Extent, extentIfBoth: Extent): (Drawable, Drawable) = {
    if (a != EmptyDrawableLater && b == EmptyDrawableLater) {
      (a(extentIfOne), EmptyDrawable())
    } else if (a == EmptyDrawableLater && b != EmptyDrawableLater) {
      (EmptyDrawable(), b(extentIfOne))
    } else if (a != EmptyDrawableLater && b != EmptyDrawableLater) {
      (a(extentIfBoth), b(extentIfBoth))
    } else {
      (EmptyDrawable(), EmptyDrawable())
    }
  }

  private val centerExtent = preferredSizeOfCenter // <- for now, the preferred size of the center is the Word.
  val _center: Drawable = center(centerExtent)

  private val heightRemaining = extent.height - centerExtent.height
  private val widthRemaining = extent.width - centerExtent.width

  val (_top: Drawable, _bottom: Drawable) = createParallelBorderDrawables(top, bottom,
    Extent(centerExtent.width, heightRemaining), Extent(centerExtent.width, heightRemaining / 2.0))
  val (_left: Drawable, _right: Drawable) = createParallelBorderDrawables(left, right,
    Extent(widthRemaining, centerExtent.height), Extent(widthRemaining / 2.0, centerExtent.height))

  // Smash together the all the drawables!
  private val layout = _bottom transX _left.extent.width below ((_left beside _center beside _right) below
    (_top transX _left.extent.width))

  override def draw(canvas: CanvasRenderingContext2D): Unit = layout.draw(canvas)
}
