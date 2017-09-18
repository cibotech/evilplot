package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Rect, WrapDrawable}
import com.cibo.evilplot.plot.Chart.ChartRendertimeSpaceException
import com.cibo.evilplot.plotdefs.PlotOptions
object Chart {
  case class ChartRendertimeSpaceException(msg: String = "") extends Throwable(msg)
}
trait Chart extends WrapDrawable {
  val options: PlotOptions
  val chartSize: Extent
  override lazy val extent: Extent = chartSize
//  protected def chartBackground: Drawable = Rect(chartAreaSize) filled options.backgroundColor

  protected lazy val xAxis: Drawable = EmptyDrawable()
  protected lazy val yAxis: Drawable = EmptyDrawable()
  protected lazy val chartAreaSize: Extent = chartSize

  // A class extending chart must define how to create its plotted data as a drawable.
  protected def plottedData(extent: Extent): Drawable
  protected def background: Drawable = Rect(chartAreaSize) filled options.backgroundColor
  // name has an underscore for compatibility, change this.
  def chartArea: Drawable = {
//    println("Constructing the chart area with size", chartAreaSize)
//    println("My background has size", background.extent)
    background behind plottedData(chartAreaSize)
  }
  protected def ensureSpace(r: => Drawable)(msg: String = "not enough space to render plot"): Drawable = {
    if (chartAreaSize.width <= 0 || chartAreaSize.height <= 0) throw new ChartRendertimeSpaceException(msg)
    else r
  }
  override def drawable: Drawable = ensureSpace {
    yAxis beside (xAxis /*transX yAxis.extent.width*/ below chartArea)
  } ("not enough space to render plot")
}

