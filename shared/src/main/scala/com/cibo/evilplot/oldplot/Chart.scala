package com.cibo.evilplot.oldplot

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.oldplot.Chart.ChartRendertimeSpaceException
import com.cibo.evilplot.plotdefs.PlotOptions

trait Chart {
  val options: PlotOptions
  val chartSize: Extent
  private val paddingHack: Double = 10 // still needs a small one due to text
  lazy val extent: Extent = Extent(chartSize.width + rightLabel.extent.width + paddingHack,
    chartSize.height + topLabel.extent.height + paddingHack)
//  protected def chartBackground: Drawable = Rect(chartAreaSize) filled options.backgroundColor

  protected lazy val xAxis: Drawable = EmptyDrawable()
  protected lazy val yAxis: Drawable = EmptyDrawable()
  protected lazy val chartAreaSize: Extent = chartSize
  // A class extending chart must define how to create its plotted data as a drawable.
  protected def plottedData(extent: Extent): Drawable
  protected def background: Drawable = Rect(chartAreaSize) filled options.backgroundColor
  def chartArea: Drawable = {
    background behind plottedData(chartAreaSize)
  }
  protected def ensureSpace(r: => Drawable)(msg: String = "not enough space to render plot"): Drawable = {
    if (chartAreaSize.width <= 0 || chartAreaSize.height <= 0) throw ChartRendertimeSpaceException(msg)
    else r
  }

  protected lazy val rightLabel: Drawable = EmptyDrawable()
  protected lazy val topLabel: Drawable = EmptyDrawable()
  def drawable: Drawable = ensureSpace {
    (yAxis beside (xAxis below (chartArea beside rightLabel) below topLabel transY -topLabel.extent.height)) transY
      (topLabel.extent.height + paddingHack / 2)
  } ("not enough space to render plot")
}

object Chart {
  case class ChartRendertimeSpaceException(msg: String = "") extends Throwable(msg)
}

