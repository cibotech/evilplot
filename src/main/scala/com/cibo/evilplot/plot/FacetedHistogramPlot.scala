/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Text
import com.cibo.evilplot.colors.{Color, HSL}
import com.cibo.evilplot.geometry.{Align, Drawable, Extent, Rect, WrapDrawable}
import com.cibo.evilplot.numeric.Histogram

import scala.collection.immutable.{SortedMap, TreeMap}


// A FacetedHistogramPlot is like facets in ggplot2. Divide the `data` in subsets according to `categories`.
// `data` and `categories` must have the same length. Each entry in `categories` is a label for the entry at the same
// position in `data`.
// TODO: generalize faceting beyond histograms.
// LATER: generalize `categories` to type T, not just String. Consider sort order.


class FacetedHistogramPlot(extent: Extent, xBounds: Option[(Double, Double)], data: Seq[Seq[Double]], numBins: Int,
                           title: Option[String] = None, categories: Seq[String],
                           optionsByCategory: Map[String, Seq[PlotOptions]])
  extends WrapDrawable {
  for (_data <- data) require(_data.length == categories.length)


  private val _drawable: Drawable = {
    // For each Seq in `data`, construct a set of horizontal bar charts faceted by category
    val chartSpacing = 5
    // See https://stackoverflow.com/questions/6833501/efficient-iteration-with-index-in-scala
    val charts = (for ((_data, index) <- data.view.zipWithIndex) yield {
      // Partition the data according to categories. Create a histogram for each partition. Sort by category.
      val catMap: Map[String, Seq[Double]] =
        (categories zip _data).groupBy(_._1)
          .map {case (category: String, labeledData: Seq[(String, Double)]) => category -> labeledData.map(_._2)}
      val histMap: Map[String, Histogram] = catMap.mapValues(new Histogram(_, numBins))
      val sortedHistMap: SortedMap[String, Histogram] = TreeMap(histMap.toArray: _*)

      // Create subcharts horizontally with spacing between them
      // TODO: Bar charts with the same bar width still have scaling issues.
      val nCharts = sortedHistMap.size
      val totalChartSpacing = (nCharts - 1) * chartSpacing
      val subchartWidth = (extent.width - totalChartSpacing) / nCharts
      require(subchartWidth > chartSpacing, "FacetedHistogramPlot: not enough horizontal space")
      (for {
        (category, hist) <- sortedHistMap
        histData = hist.bins.map(_.toDouble)
        xBounds = Some(Bounds(hist.min, hist.max))
        options = optionsByCategory(category)(index)
//        y = println(category, histData)
        chart = new BarChart(Extent(subchartWidth, extent.height), xBounds, histData, options)
        titledChart = chart
        // if (true) chart beside horizontalLabel("type of error", chart.layout._center.extent.height) else chart
      } yield titledChart)
        .toSeq
        .seqDistributeH(chartSpacing)
    })
    // Stack the horizontal charts vertically. Reverse the order so as to fill in from the bottom up.
      .reverse.seqDistributeV(chartSpacing) // below verticalLabel("crop name", 0.5 * (extent.width - 5))
    title match {
      case Some(_title) => charts titled (_title, 20) padAll 10
      case None => charts
    }
  }

  def horizontalLabel(message: String, height: Double, color: Color = HSL(0, 0, 85)): Drawable = {
    val text = Text(message) rotated 90
    Align.centerSeq(Align.middle(Rect(2 * text.extent.width, height) filled color, text)).group
  }

  def verticalLabel(message: String, width: Double, color: Color = HSL(0, 0, 85)): Drawable = {
    val text = Text(message)
    Align.centerSeq(Align.middle(Rect(width, 2 * text.extent.height) filled color, text)).group
  }

  override def drawable: Drawable = _drawable
}
