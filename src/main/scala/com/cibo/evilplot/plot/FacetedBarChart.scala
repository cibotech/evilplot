/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, WrapDrawable}


// A FacetedBarChart is like facets in ggplot2. Divide the `data` in subsets according to `categories`.
// `data` and `categories` must have the same length. Each entry in `categories` is a label for the entry at the same
// position in `data`.
// TODO: generalize faceting beyond bar charts.
// LATER: generalize `categories` to type T, not just String. Consider sort order.
class FacetedBarChart(extent: Extent, xBounds: Option[(Double, Double)], data: Seq[Double],
  title: Option[String] = None, vScale: Double = 1.0, withinMetrics: Option[Double], categories: Seq[String])
  extends WrapDrawable {
  require(data.length == categories.length)
  private val _drawable: Drawable = {
    val labels = categories.distinct.sorted
    // Partition the data according to categories
    val subsets: Map[String, Seq[Double]] = (categories zip data).groupBy(_._1).map {case (x, y) => x -> y.map(_._2)}
    val nCharts = subsets.size

    // Carve up the horizontal space, with spacing between the subcharts
    val chartSpacing = 5
    val totalChartSpacing = (nCharts - 1) * chartSpacing
    val subchartWidth = (extent.width - totalChartSpacing) / nCharts
    require(subchartWidth > chartSpacing, "FacetedBarChart: not enough horizontal space")

    val charts: Drawable = subsets.map { case (label, subset) =>
      new BarChart(Extent(subchartWidth, extent.height), xBounds, subset, title, vScale, withinMetrics) titled label
    }.toSeq.seqDistributeH(chartSpacing)
    title match {
      case Some(_title) => charts titled (_title, 20) padAll 10
      case None => charts
    }
  }

  override def drawable: Drawable = _drawable
}
