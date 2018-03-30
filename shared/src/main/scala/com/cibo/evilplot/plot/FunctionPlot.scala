package com.cibo.evilplot.plot

import com.cibo.evilplot.numeric.Bounds
import com.cibo.evilplot.plot.aesthetics.Theme

object FunctionPlot {
  val DefaultBounds: Bounds = Bounds(0, 1)
  val DefaultNumPoints: Int = 100

  /** Plot a function.
    *
    * @param function  the function to plot.
    * @param xbounds   Range of values to use as input to the function.
    * @param numPoints Number of values to give to the function.
    */
  def apply(function: Double => Double,
    xbounds: Option[Bounds] = None,
    numPoints: Option[Int] = None)(implicit theme: Theme): Plot = {
    EmptyPlot().xbounds(xbounds.getOrElse(DefaultBounds)).function(function)
  }
}
