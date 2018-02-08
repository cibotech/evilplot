package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.renderers.PlotRenderer
import com.cibo.evilplot.DOMInitializer
import org.scalatest.{FunSpec, Matchers}

class PlotSpec extends FunSpec with Matchers {

  DOMInitializer.init()

  // Renderer to do nothing.
  private case object EmptyPlotRenderer extends PlotRenderer[Int] {
    def render(plot: Plot[Int], plotExtent: Extent): Drawable = EmptyDrawable(plotExtent)
  }

  // Renderer to get the plot extent.
  private case class PlotExtentPlotRenderer() extends PlotRenderer[Int] {
    var plotExtentOpt: Option[Extent] = None

    def render(plot: Plot[Int], plotExtent: Extent): Drawable = {
      plotExtentOpt = Some(plotExtent)
      EmptyDrawable(plotExtent)
    }
  }

  private def newPlot(
    value: Int = 0,
    xbounds: Bounds = Bounds(0, 1),
    ybounds: Bounds = Bounds(0, 1),
    renderer: PlotRenderer[Int] = EmptyPlotRenderer
  ): Plot[Int] = Plot[Int](value, xbounds, ybounds, renderer)

  it("should have the right extent") {
    val plot = newPlot()
    val extent = Extent(300, 400)
    plot.render(extent).extent shouldBe extent
  }

  it("should render the full plot area") {
    val extent = Extent(10, 20)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer)
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt shouldBe Some(extent)
  }

  it("text should reduce the size of the plot area") {
    val extent = Extent(100, 200)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer).title("Test")
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt.get.height should be < extent.height
    renderer.plotExtentOpt.get.width shouldBe extent.width
  }

  it("a background should not affect the size of the plot area") {
    val extent = Extent(300, 200)
    val renderer = PlotExtentPlotRenderer()
    val plot = newPlot(renderer = renderer).background()
    plot.render(extent).extent shouldBe extent
    renderer.plotExtentOpt.get shouldBe extent
  }

}
