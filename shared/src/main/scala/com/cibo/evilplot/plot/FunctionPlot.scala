package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Color
import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.components.FunctionPlotLine
import com.cibo.evilplot.plot.renderers.{PathRenderer, PointRenderer}

object FunctionPlot {
  val defaultBounds: Bounds = Bounds(0, 1)
  val defaultNumPoints: Int = 800

  /** Plot a function.
    * @param function  the function to plot.
    * @param xbounds   Range of values to use as input to the function.
    * @param numPoints Number of values to give to the function.
    */
  def apply(function: Double => Double,
    xbounds: Option[Bounds] = None,
    numPoints: Option[Int] = None,
    pathRenderer: Option[PathRenderer] = None,
    pointRenderer: Option[PointRenderer] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None)(implicit theme: Theme): Plot = {
    require(numPoints.forall(_ != 0), "Cannot make a function plot using zero points.")
    val pts = FunctionPlotLine.pointsForFunction(function,
      xbounds.getOrElse(defaultBounds),
      numPoints.getOrElse(defaultNumPoints))


    XyPlot(pts, pointRenderer.orElse(Some(PointRenderer.empty())), pathRenderer, xBoundBuffer, yBoundBuffer)
  }

  /** Plot a function using a name for the legend. */
  def series(function: Double => Double,
    name: String,
    color: Color,
    xbounds: Option[Bounds] = None,
    numPoints: Option[Int] = None,
    strokeWidth: Option[Double] = None,
    xBoundBuffer: Option[Double] = None,
    yBoundBuffer: Option[Double] = None
    )(implicit theme: Theme): Plot = {
    val renderer = Some(PathRenderer.named(name, color, strokeWidth))
    apply(function, xbounds, numPoints, renderer, None, xBoundBuffer, yBoundBuffer)
  }
}
