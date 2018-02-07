package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.PlotRenderer
import org.scalatest.{FunSpec, Matchers}

class PlotSpec extends FunSpec with Matchers {

  private case object EmptyPlotRenderer extends PlotRenderer[Int] {
    def render(plot: Plot[Int], plotExtent: Extent): Drawable = EmptyDrawable(plotExtent)
  }

  def newPlot(
    value: Int = 0,
    xbounds: Bounds = Bounds(0, 1),
    ybounds: Bounds = Bounds(0, 1),
    renderer: PlotRenderer[Int] = EmptyPlotRenderer
  ): Plot[Int] = Plot[Int](value, xbounds, ybounds, renderer)

  it("should render the full plot area") {
    var renderedExtent: Option[Extent] = None

    case object TestRenderer extends PlotRenderer[Int] {
      def render(plot: Plot[Int], plotExtent: Extent): Drawable = {
        renderedExtent = Some(plotExtent)
        EmptyDrawable(plotExtent)
      }
    }

    val extent = Extent(10, 20)
    newPlot(renderer = TestRenderer).render(extent).extent shouldBe extent
    renderedExtent shouldBe Some(extent)
  }

}
