/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.demo

import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot
import com.cibo.evilplot.plot._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.components.{Marker, Position}
import com.cibo.evilplot.plot.renderers.{BarRenderer, PathRenderer, PointRenderer}

import scala.util.Random

/** A number of examples of Evil Plotting */
object DemoPlots {
  implicit val theme: DefaultTheme = DefaultTheme().copy(
    fonts = DefaultFonts()
      .copy(tickLabelSize = 14, legendLabelSize = 14, fontFace = "'Lato', sans-serif")
  )

  val plotAreaSize: Extent = Extent(1000, 600)
  lazy val histogram: Drawable = {
    val data = (0.0 to 3 by .25) ++ (3.0 to 5 by .05) ++ (5.0 to 8 by 1.0)
    plot
      .Histogram(data, 10)
      .standard()
      .xbounds(-75, 225)
      .ybounds(0, 15)
      .vline(3.5, HTMLNamedColors.blue)
      .render(plotAreaSize)
  }

  lazy val barChart: Drawable = {
    val percentChange = Seq[Double](-10, 5, 12, 68, -22)
    val labels = Seq("one", "two", "three", "four", "five")

    def labeledByColor(implicit theme: Theme) = new BarRenderer {
      def render(plot: Plot, extent: Extent, category: Bar): Drawable = {
        val rect = Rect(extent)
        val value = category.values.head
        val positive = HEX("#4c78a8")
        val negative = HEX("#e45756")
        val color = if (value >= 0) positive else negative
        Align
          .center(
            rect filled color,
            Text(s"$value%", fontFace = theme.fonts.fontFace, size = 20).filled(theme.colors.label))
          .group
      }
    }

    BarChart
      .custom(percentChange.map(Bar.apply), spacing = Some(20), barRenderer = Some(labeledByColor))
      .standard(xLabels = labels)
      .hline(0)
      .render(plotAreaSize)
  }

  lazy val pieChart: Drawable = {
    val data = Seq("one" -> 1.5, "two" -> 3.5, "three" -> 2.0)
    PieChart(data).rightLegend().render(plotAreaSize)
  }

  lazy val linePlot: Drawable = {
    val data = (0 to 5)
      .map(_.toDouble)
      .zip(
        Seq(
          0.0, 0.1, 0.0, 0.1, 0.0, 0.1
        ))
      .map(Point.tupled)

    LinePlot(
      data
    ).ybounds(0, .12)
      .yAxis()
      .xGrid()
      .yGrid()
      .frame()
      .render(plotAreaSize)
  }

  lazy val scatterPlot: Drawable = {
    val points = Seq.fill(150)(Point(Random.nextDouble(), Random.nextDouble())) :+ Point(0.0, 0.0)
    val years = Seq.fill(150)(Random.nextDouble()) :+ 1.0
    val pointsWithYears = years.zip(points).groupBy(_._1).mapValues(_.map(_._2)).toSeq.sortBy(_._1)
    ScatterPlot(points, pointRenderer = Some(PointRenderer.depthColor(years, None, None)))
      .frame()
      .xGrid()
      .yGrid()
      .xAxis()
      .yAxis()
      .xLabel("x")
      .yLabel("y")
      .trend(1, 0)
      .rightLegend()
      .render(plotAreaSize)
  }

  lazy val heatmap: Drawable = {
    val data = Seq[Seq[Double]](
      Seq(1, 2, 3, 4),
      Seq(5, 6, 7, 8),
      Seq(9, 8, 7, 6)
    )

    Heatmap(data).title("Heatmap Demo").xAxis().yAxis().rightLegend().render(plotAreaSize)
  }

  lazy val clusteredBarChart: Drawable = {
    val data = Seq[Seq[Double]](
      Seq(1, 2, 3),
      Seq(4, 5, 6),
      Seq(3, 4, 1),
      Seq(2, 3, 4)
    )
    BarChart
      .clustered(
        data,
        labels = Seq("one", "two", "three")
      )
      .title("Clustered Bar Chart Demo")
      .xAxis(Seq("a", "b", "c", "d"))
      .yAxis()
      .frame()
      .bottomLegend()
      .render(plotAreaSize)
  }

  lazy val stackedBarChart: Drawable = {
    val data = Seq[Seq[Double]](
      Seq(1, 2, 3),
      Seq(4, 5, 6),
      Seq(3, 4, 1),
      Seq(2, 3, 4)
    )
    BarChart
      .stacked(
        data,
        labels = Seq("one", "two", "three")
      )
      .title("Stacked Bar Chart Demo")
      .xAxis(Seq("a", "b", "c", "d"))
      .yAxis()
      .frame()
      .bottomLegend()
      .render(plotAreaSize)
  }

  lazy val clusteredStackedBarChart: Drawable = {
    val data = Seq[Seq[Seq[Double]]](
      Seq(Seq(1, 2, 3), Seq(4, 5, 6)),
      Seq(Seq(3, 4, 1), Seq(2, 3, 4))
    )
    BarChart
      .clusteredStacked(
        data,
        labels = Seq("one", "two", "three")
      )
      .title("Clustered Stacked Bar Chart Demo")
      .xAxis(Seq("Category 1", "Category 2"))
      .yAxis()
      .xGrid()
      .yGrid()
      .xLabel("Category")
      .yLabel("Level")
      .frame()
      .rightLegend()
      .render(plotAreaSize)
  }

  lazy val facetedPlot: Drawable = {
    val years = 2007 to 2013
    val datas: Seq[Seq[Point]] =
      years.map(_ => Seq.fill(Random.nextInt(20))(Point(Random.nextDouble(), Random.nextDouble())))
    val plot1 = Overlay(ScatterPlot.series(datas(0), "2010", HTMLNamedColors.red))
    val plot2 = Overlay(
      ScatterPlot.series(datas(0), "2010", HTMLNamedColors.red),
      ScatterPlot.series(datas(1), "2011", HTMLNamedColors.blue)
    )
    val plot3 = Overlay(
      ScatterPlot.series(datas(0), "2010", HTMLNamedColors.red),
      ScatterPlot.series(datas(1), "2011", HTMLNamedColors.blue),
      ScatterPlot.series(datas(2), "2012", HTMLNamedColors.green),
      ScatterPlot.series(datas(3), "2013", HTMLNamedColors.teal)
    )
    val plot4 = Overlay(
      ScatterPlot.series(datas(0), "2010", HTMLNamedColors.red)
    )

    Facets(Seq(Seq(plot1, plot2), Seq(plot3, plot4)))
      .frame()
      .xAxis()
      .yAxis()
      .xLabel("x")
      .yLabel("y")
      .xGrid()
      .yGrid()
      .trend(1.0, 0)
      .topLabels(Seq("A", "B"))
      .title("Facet Demo")
      .rightLegend()
      .rightLabels(Seq("before", "after"))
      .render(Extent(600, 400))
  }

  lazy val marginalHistogram: Drawable = {
    import com.cibo.evilplot.plot._
    import com.cibo.evilplot.plot.renderers._

    // Make up some data...
    val allYears = (2007 to 2013).toVector
    val data = Seq.fill(150)(Point(Random.nextDouble(), Random.nextDouble()))
    val years = Seq.fill(150)(allYears(Random.nextInt(allYears.length)))

    val xhist = Histogram(data.map(_.x), bins = 50)
    val yhist = Histogram(data.map(_.y), bins = 40)
    ScatterPlot(
      data = data,
      pointRenderer = Some(PointRenderer.colorByCategory(years))
    ).frame()
      .topPlot(xhist)
      .rightPlot(yhist)
      .xGrid()
      .yGrid()
      .xAxis()
      .yAxis()
      .xLabel("x")
      .yLabel("y")
      .trend(1, 0, color = RGB(45, 45, 45), lineStyle = LineStyle.DashDot)
      .overlayLegend(x = 0.95, y = 0.8)
      .render(plotAreaSize)
  }

  lazy val crazyPlot: Drawable = {
    import com.cibo.evilplot.plot._
    import com.cibo.evilplot.plot.renderers._

    // Make up some data...
    val allYears = (2007 to 2013).toVector
    val data = Seq.fill(150)(Point(6 * Random.nextDouble(), Random.nextDouble()))
    val years = Seq.fill(150)(allYears(Random.nextInt(allYears.length)))

    val xhist = Histogram(data.map(_.x), bins = 50)
    val yhist = Histogram(data.map(_.y), bins = 40)
    val plot = ScatterPlot(
      data = data,
      pointRenderer = Some(PointRenderer.colorByCategory(years))
    ).xAxis()
      .topPlot(xhist)
      .rightPlot(yhist)
      .rightLegend()
      .overlayLegend(x = 0.95, y = 0.8)
      .bottomLegend()

    val lines = Overlay(
      LinePlot.series(Seq(Point(2000, 0), Point(10000, 10000)), "one", HTMLNamedColors.red),
      LinePlot.series(Seq(Point(2000, 10000), Point(10000, 0)), "two", HTMLNamedColors.blue)
    ).xbounds(0, 10000).ybounds(0, 10000).overlayLegend(0.75, 0.5)

    val plot2 = Histogram(Seq(1, 1, 1.5, 1, 5, 3, 2, 5, 7, 8, 9, 10), bins = 8)
      .ybounds(0, 10)
      .xbounds(0, 10)
      .yAxis()
    val plot3 = BarChart.custom(
      bars = Seq(
        Bar(Seq(0.5, 0.8), 0, Color.stream),
        Bar(Seq(0.2, 0.7), 0, Color.stream),
        Bar(Seq(0.4, 0.9), 1, Color.stream.drop(5)),
        Bar(Seq(0.1, 0.3), 1, Color.stream.drop(5))
      ),
      barRenderer = Some(BarRenderer.stacked())
    )
    val plot4 = plot3.overlay(plot2)
    val plot5 = plot3.xAxis(Seq("one", "two", "four", "six"), Seq(1.0, 2.0, 4.0, 6.0)).yAxis()
    val facets = Facets(
      Seq(
        Seq(
          plot
            .vline(3.2)
            .hline(.6)
            .function(
              d => math.pow(d, 3),
              color = HTMLNamedColors.dodgerBlue,
              lineStyle = LineStyle.Dotted),
          plot5),
        Seq(lines, plot4)
      )
    ).title("Actual vs. Expected")
      .xLabel("Actual")
      .yLabel("Expected")
      .rightLabels(Seq("row one", "row two"))
      .rightLegend()
      .bottomLegend()
      .rightLabel((e: Extent) => Rect(e) filled HTMLNamedColors.blue, 10)
      .leftLabel((e: Extent) => Rect(e) filled HTMLNamedColors.green, 10)
      .topLabel((e: Extent) => Rect(e) filled HTMLNamedColors.red, 10)
      .bottomLabel((e: Extent) => Rect(e) filled HTMLNamedColors.yellow, 10)
      .xAxis()
      .yAxis()
      .frame()
      .xGrid()
      .yGrid()
    facets.render(plotAreaSize)
  }

  lazy val boxPlot: Drawable = {
    val data = Seq.fill(10)(Seq.fill(Random.nextInt(30))(Random.nextDouble()))
    BoxPlot(data)
      .standard(xLabels = (1 to 10).map(_.toString))
      .render(plotAreaSize)
  }

  lazy val functionPlot: Drawable = {
    val Seq(one, two, three) = theme.colors.stream.take(3)
    Overlay(
      FunctionPlot.series(x => x * x, "y = x\u00B2", one, xbounds = Some(Bounds(-1, 1))),
      FunctionPlot.series(x => math.pow(x, 3), "y = x\u00B3", two, xbounds = Some(Bounds(-1, 1))),
      FunctionPlot.series(x => math.pow(x, 4), "y = x\u2074", three, xbounds = Some(Bounds(-1, 1)))
    ).xLabel("x")
      .yLabel("y")
      .overlayLegend()
      .standard()
      .title("A bunch of polynomials.")
      .render(plotAreaSize)
  }

  lazy val markerPlot: Drawable = {
    val Seq(one, two, three) = theme.colors.stream.take(3)
    FunctionPlot
      .series(x => x, "y = x", one, xbounds = Some(Bounds(-1, 1)))
      .xLabel("x")
      .yLabel("y")
      .overlayLegend()
      .standard()
      .component(Marker(Position.Overlay, _ => Style(Rect(25), three), Extent(25, 25), 0, 0))
      .component(
        Marker(
          Position.Overlay,
          _ => Style(Text(" Square marker at the center", 20), three),
          Extent(25, 25),
          0,
          -0.1))
      .component(
        Marker(Position.Top, _ => Style(Rotate(Wedge(40, 25), 250), two), Extent(25, 25), 0.7))
      .component(
        Marker(Position.Top, _ => Style(Text(" Up here is a wedge", 20), two), Extent(25, 25), 0.7))
      .title("A line graph with markers")
      .render(plotAreaSize)
  }

  def gaussianKernel(u: Double): Double = {
    1 / math.sqrt(2 * math.Pi) * math.exp(-0.5d * u * u)
  }

  def densityEstimate(data: Seq[Double], bandwidth: Double)(x: Double): Double = {
    val totalProbDensity = data.map { x_i =>
      gaussianKernel((x - x_i) / bandwidth)
    }.sum
    totalProbDensity / (data.length * bandwidth)
  }

  lazy val densityPlot: Drawable = {
    val data = Seq.fill(150)(Random.nextDouble() * 30)
    val colors = theme.colors.stream.take(3)
    val bandwidths = Seq(5d, 2d, 0.5d)
    Overlay(
      colors.zip(bandwidths).map {
        case (c, b) =>
          FunctionPlot(
            densityEstimate(data, b),
            Some(Bounds(0, 30)),
            Some(500),
            Some(PathRenderer.default(color = Some(c)))
          )
      }: _*
    ).standard()
      .xbounds(0, 30)
      .render(plotAreaSize)
  }

  lazy val contourPlot: Drawable = {
    import com.cibo.evilplot.plot._

    import scala.util.Random

    val data = Seq.fill(100)(Point(Random.nextDouble() * 20, Random.nextDouble() * 20))
    ContourPlot(data)
      .standard()
      .xbounds(0, 20)
      .ybounds(0, 20)
      .render(plotAreaSize)
  }
}
