/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent, WrapDrawable}
import com.cibo.evilplot.numeric.Histogram

import scala.collection.immutable
import scala.collection.immutable.{SortedMap, TreeMap}


// A FacetedHistogramPlot is like facets in ggplot2. Divide the `data` in subsets according to `categories`.
// `data` and `categories` must have the same length. Each entry in `categories` is a label for the entry at the same
// position in `data`.
// TODO: generalize faceting beyond histograms.
// LATER: generalize `categories` to type T, not just String. Consider sort order.

// Could eventually use this for all plots, simplifying their constructors?
case class PlotOptions(xAxisBounds: Option[(Double, Double)] = None, yAxisBounds: Option[(Double, Double)] = None,
                       annotation: Option[ChartAnnotation] = None, xGridSpacing: Option[Double] = None,
                       yGridSpacing: Option[Double] = None, barWidth: Option[Double] = None)

class FacetedHistogramPlot(extent: Extent, xBounds: Option[(Double, Double)], data: Seq[Double], numBins: Int,
                           title: Option[String] = None, categories: Seq[String],
                           optionsByCategory: Map[String, PlotOptions])
  extends WrapDrawable {
  require(data.length == categories.length)
  private val _drawable: Drawable = {
    // Partition the data according to categories. Create a histogram for each partition. Sort by category.
    val catMap: Map[String, Seq[Double]] =
      (categories zip data).groupBy(_._1)
        .map {case (category: String, labeledData: Seq[(String, Double)]) => category -> labeledData.map(_._2)}
    val histMap: Map[String, Histogram] = catMap.mapValues(new Histogram(_, numBins))
    val sortedHistMap: SortedMap[String, Histogram] = scala.collection.immutable.TreeMap(histMap.toArray: _*)

    // Create subcharts with spacing between them
    // TODO: Bar charts with the same bar width still have scaling issues.
    val nCharts = sortedHistMap.size
    val chartSpacing = 5
    val totalChartSpacing = (nCharts - 1) * chartSpacing
    val subchartWidth = (extent.width - totalChartSpacing) / nCharts
    require(subchartWidth > chartSpacing, "FacetedHistogramPlot: not enough horizontal space")
    val charts = (for {
      (category, hist) <- sortedHistMap
      histData = hist.bins.map(_.toDouble)
      xBounds = Some(hist.min, hist.max)
      options = optionsByCategory(category)
    } yield new BarChart(Extent(subchartWidth, extent.height), xBounds, histData,
                         xAxisDrawBounds = options.xAxisBounds,
                         yAxisDrawBounds = options.yAxisBounds,
                         xGridSpacing = options.xGridSpacing,
                         yGridSpacing = options.yGridSpacing,
                         annotation = options.annotation,
                         withinMetrics = Some(15.0),
                         title = Some(category)))
      .toSeq
      .seqDistributeH(chartSpacing)

    // Add the title if one was provided
    title match {
      case Some(_title) => charts titled (_title, 20) padAll 10
      case None => charts
    }
  }

  override def drawable: Drawable = _drawable
}
