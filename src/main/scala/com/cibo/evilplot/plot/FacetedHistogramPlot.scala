/*
 * Copyright 2017 CiBO Technologies
 */
package com.cibo.evilplot.plot

import com.cibo.evilplot.Utils
import com.cibo.evilplot.colors.White
import com.cibo.evilplot.geometry.{Drawable, DrawableLaterMaker, Extent, WrapDrawable}
import com.cibo.evilplot.layout.{ChartLayout, GridLayout}
import com.cibo.evilplot.numeric.Histogram

import scala.collection.immutable.TreeMap


// A FacetedHistogramPlot is like facets in ggplot2. Divide the `data` in subsets according to `categories`.
// `data` and `categories` must have the same length. Each entry in `categories` is a label for the entry at the same
// position in `data`.
// TODO: generalize faceting beyond histograms.
// LATER: generalize `categories` to type T, not just String. Consider sort order.
// LATER: Right now, categories are columns and rows are metrics. Consider allowing the opposite.

/**
  * @param data a sequence of datasets; each sequence inside is the data for a different metric. All sequences in data
  *             must have the same length.
  * @param categories each entry is a label for the entry at the same position in each sequence of `data`.
  * @param metricNames labels for the sequences of `data`. If defined, must have the same number of entries as `data`
*/
class FacetedHistogramPlot(extent: Extent, data: Seq[Seq[Double]], numBins: Int, title: Option[String] = None,
                           categories: Seq[String], optionsByCategory: Map[String, Seq[PlotOptions]],
                           xLabel: Option[String] = None, yLabel: Option[String] = None,
                           metricNames: Option[Seq[String]] = None) extends WrapDrawable {
  // Check arguments
  for (_data <- data) require(_data.length == categories.length)
  if (metricNames.isDefined) require(metricNames.get.length == data.length)


  private val allCharts: Drawable= {
    def makeChart(xBounds: Option[Bounds], data: Seq[Double], options: PlotOptions)(extent: Extent): Drawable = {
      new HistogramChart(extent, xBounds, data, options)
    }

    // Each element in the Seq corresponds to a Seq[Double] in data, but split by category.
    val groupedByCategory: Seq[Map[String, Seq[Double]]] = for { dataSet <- data
      categoryMap = (dataSet zip categories).groupBy{ case (_, category) => category } // group by category
                            .mapValues(_. map { case (datum, _) => datum })  // but don't want a (datum, category) pair
      sortedMap = TreeMap(categoryMap.toArray: _*)
    } yield sortedMap

    val sortedCategories: Iterable[String] = groupedByCategory.head.keys

    // Modify the chart options for each facet: only the top row should be labeled by category. Only the bottom
    // should have x-axes. Only the left column should have y-axes. Only the right column should have metric name.
    def buildOptions(category: String, catIdx: Int, dataSetIdx: Int): PlotOptions = {
      val suppliedOptions = optionsByCategory(category)(dataSetIdx)
      val xAxisOptions = if (dataSetIdx == 0) suppliedOptions.copy(drawXAxis = true)
      else suppliedOptions.copy(drawXAxis = false, topLabel = Some(category))
      val yAxisOptions = if (catIdx != 0) xAxisOptions.copy(drawYAxis = false) else xAxisOptions.copy(drawYAxis = true)
      metricNames match {
        case Some(names) if catIdx == sortedCategories.size - 1 =>
          yAxisOptions.copy(rightLabel = Some(names(dataSetIdx)))
        case _ => yAxisOptions
      }
    }

    // Each category gets binned across the same x-range, so we need to find extrema by category.
    val categoryBounds: Map[String, Bounds] = (for {
      category <- sortedCategories
      allValuesForCategory: Seq[Double] = groupedByCategory.flatMap(_(category))
      bounds = Bounds(allValuesForCategory.min, allValuesForCategory.max)
    } yield category -> bounds)(scala.collection.breakOut)

    val plots: Iterable[DrawableLaterMaker] = for {
      (category, catIdx) <- sortedCategories.zipWithIndex
      (dataSetMap, dataSetIdx) <- groupedByCategory.zipWithIndex
      _data = dataSetMap(category)
      xBounds = Some(categoryBounds(category))
      hist = new Histogram(_data, numBins, bounds = xBounds)
      histData = hist.bins.map(_.toDouble)
      options = buildOptions(category, catIdx, dataSetIdx)
    } yield new DrawableLaterMaker(makeChart(xBounds, histData, options))

    new GridLayout(extent, data.length, sortedCategories.size, plots.toSeq)
  }
  private def drawCharts(e: Extent): Drawable = allCharts
  val _allCharts = new DrawableLaterMaker(drawCharts)

  private val layout = {
    val _xLabel = Utils.maybeDrawableLater(xLabel,
      (label: String) => Label(label, textSize = Some(20), color = White, rotate = 270))
    val _yLabel = Utils.maybeDrawableLater(yLabel,
      (label: String) => Label(label, textSize = Some(20), color = White))

    new ChartLayout(allCharts.extent, preferredSizeOfCenter = allCharts.extent, center = _allCharts, bottom = _yLabel,
      left = _xLabel)
  }
  val finalChart: Drawable = Utils.maybeDrawable(title, (_title: String) => layout titled (_title, 20),
    default = layout)
  override def drawable: Drawable = finalChart
}
