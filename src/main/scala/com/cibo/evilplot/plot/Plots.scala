package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.Colors.{ColorSeq, GradientColorBar}
import com.cibo.evilplot.{Text, colors}
import com.cibo.evilplot.colors._
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.Histogram


object Plots {

  def createHistogramPlot(
    size: Extent, data: Seq[Double], numBins: Int, title: Option[String] = None,
    annotation: Option[ChartAnnotation], vScale: Double = 1.0): Drawable = {
    val hist = new Histogram(data, numBins)
    val graphData: Seq[Double] = hist.bins.map(_.toDouble)
    new BarChart(size, Some(hist.min, hist.max), graphData, title, vScale, Some(15), annotation)
  }

  def createScatterPlot(graphSize: Extent, data: Seq[Point], zData: Seq[Double], nColors: Int): Pad = {
    val minX = data.minBy(_.x).x
    val maxX = data.maxBy(_.x).x
    val minY = data.minBy(_.y).y
    val maxY = data.maxBy(_.y).y

    val pointSize = 2
    val textSize = 24
    val scalex = graphSize.width / (maxX - minX)
    val scaley = graphSize.height / (maxY - minY)
    val colorBar = GradientColorBar(nColors, zData.min, zData.max)

    val fitScatter = FlipY(Fit(graphSize) {
      val scatter = (data zip zData).map { case (Point(x, y), zVal) =>
        Disc(pointSize, (x - math.min(0, minX)) * scalex, (y - math.min(0, minY)) * scaley) filled colorBar.getColor(zVal) }.group
      val xAxis = axis(graphSize, true, maxX, textSize, minX)
      val pointAndY = FlipY(axis(graphSize, false, maxY, textSize, minY)) beside scatter
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    // TODO: Generate the labels from the given data.
    val labels = Seq[Int](2007, 2008, 2009, 2010, 2011, 2012, 2013, 2014, 2015)
    val legend = distributeV(
      labels.zip(colorBar.colors).map { case (l, c) =>
        Disc(3) filled c labeled f"$l%4d" }, 10
    ) padLeft(10)
    fitScatter padAll 10 beside legend titled ("A Scatter Plot", 20) padAll 10
  }

  def createLinePlot(graphSize: Extent, data: Seq[Point]): Pad = {
    val textSize = 24

    val minX = data.minBy(_.x).x
    val maxX = data.maxBy(_.x).x
    val minY = data.minBy(_.y).y
    val maxY = data.maxBy(_.y).y

    val fitLine = FlipY(Fit(graphSize) {
      val scalex = graphSize.width / (maxX - minX)
      val scaley = graphSize.height / (maxY - minY)
      val line = Segment(data.map(p => Point(p.x * scalex, p.y * scaley)), 0.5)
      val xAxis = axis(graphSize, true, maxX, textSize, minX)
      val pointAndY = FlipY(axis(graphSize, false, maxY, textSize, minY)) beside line
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    fitLine titled ("A Line Plot", 20) padAll 10
  }

  def createMultiLinePlot(graphSize: Extent, datas: Seq[Seq[Point]]): Pad = {
    val fitLine = FlipY(Fit(graphSize) {
      val lines = datas.map(data => Segment(data, 0.5))
      val xAxis = axis(graphSize, true, lines.map(_.xS.max).max, 0, lines.map(_.xS.min).min) // TODO: wut
      val pointAndY =
        FlipY(axis(graphSize, false, lines.map(_.yS.max).max, 0, lines.map(_.yS.min).min)) beside lines.group
      Align.right(pointAndY, FlipY(xAxis)).reverse.reduce(Above)
    })

    fitLine titled ("A Multi Line Plot", 20) padAll 10
  }

  def createPieChart(scale: Int, data: Seq[Double]): Pad = {
    val pieWedges = {
      val labelPad = 10 // TODO: should be maxTextWidth?

      // cumulativeRotate is complicated b/c we draw wedges straddling the X axis, but that makes labels easier
      val cumulativeRotate = data.map(_ / 2).sliding(2).map(_.sum).scanLeft(0D)(_+_).toVector
      val wedges = data.zip(cumulativeRotate).map { case (frac, cumRot) =>

        val rotate = 360 * cumRot
        val wedge = UnsafeRotate(rotate)(Wedge(360 * frac, scale))
        val label =
          UnsafeRotate(rotate) {
            val text = {
              val baseText = Text(frac.toString) filled Black
              // TODO: same as left aligned txt?
              if (rotate > 90 && rotate < 270)
                baseText transX (-baseText.extent.width - labelPad)
              else
                baseText
            }
            val spacer = Disc(scale) filled Clear padRight labelPad

            distributeH(Align.middle(spacer, UnsafeRotate(-rotate)(text) ))
          }

        wedge behind label
      }

      wedges.zip(Colors.triAnalogStream()).map { case (r, color) => r filled color }
    }.group

    val legend = flowH(
      data.zip(Colors.triAnalogStream()).map{ case (d, c) => Rect(scale / 5.0) filled c labeled f"${d*100}%.1f%%" },
      pieWedges.extent
    ) padTop 20

    pieWedges padAll 15 above legend titled("A Smooth Pie Chart", 20) padAll 10
  }

  private[plot] def createGridLines(maxHeight: Double, width: Double): Drawable =
    distributeV {
      val lineEveryXUnits     = 2
      val lineThick           = 0.25
      val textHeight          = Text.defaultSize
      val labelFloatAboveLine = 2

      val countOfGridLines = (maxHeight / lineEveryXUnits).toInt

      if (countOfGridLines > 0) {
        Seq.tabulate(countOfGridLines) { x =>
          val yHeightLabel = (maxHeight / lineEveryXUnits - x) * lineEveryXUnits

          Pad(bottom = lineEveryXUnits - lineThick) {

            val label = Translate(y = -textHeight - labelFloatAboveLine) {
              Text(yHeightLabel) filled Grey
            }

            Line(width, lineThick) behind label
          }
        }
      }
      else {
        Seq(EmptyDrawable())
      }
    }

  // TODO: this sort of sucks, and textAndPadHeight is a hack that doesn't work in my barchart case with tick labels?
  private def axis(
    graphSize: Extent, horizontal: Boolean, maxValue: Double, textAndPadHeight: Double, minValue: Double = 0,
    doLabelTicks: Boolean = true)
  : Drawable = {

    val rangeSize = maxValue - minValue
    val figureSize = if (horizontal) graphSize.width else graphSize.height
    val tickThick = 1
    val tickLength = 5
    //TODO: Fix dis, extent is being improperly used in some cases, text size should also not be dependent on the width
    // for readability reasons and the scaling is wack
    // requirement failed: Cannot use 0.096, canvas will not draw text initially sized < 0.5px even when scaling
    val textSize = textAndPadHeight.max(5.0) * 0.75
    val tickLabelTextSize = 0.8 * textSize
    val numTicks = 10
    val interTickDist = figureSize / numTicks

    val labelEveryKTicks = 2
    val ticks = Seq.tabulate(numTicks + 1) { i =>
      val tick = Line(tickLength, tickThick).rotated(90).padRight(interTickDist - tickThick)

      tick
    }.seqDistributeH

    val labels = {
      val labelCount = 1 + math.floor(numTicks / labelEveryKTicks).toInt
      val interLabelDist = interTickDist * labelEveryKTicks

      val labelColl = Seq.tabulate(labelCount) { i =>
        val scale = rangeSize / figureSize
        require(scale > 0, "scale")
        require(rangeSize > 0, "range")
        require(figureSize > 0, "figure")
        val value = i * interLabelDist * scale + minValue
        Text(f"$value%.1f", tickLabelTextSize).padRight(textSize / 4).rotated(if (horizontal) -90 else 0)
      }.reverse

      val combined = distributeV(
        labelColl,
        interLabelDist - labelColl.head.extent.height
      )

      val textCentering = tickLabelTextSize / 3
      combined //padTop textCentering
    }

    val axisTitle = Text("Awesomeness", textSize) rotated (if (horizontal) 0 else -90)
    val line = ticks behind Line(figureSize, tickThick * 2).padTop(tickLength) rotated (if (horizontal) 90 else -90)
    val labeledTickAxis = if (doLabelTicks) {
      if (horizontal)
        line behind (labels padLeft tickLabelTextSize) rotated 90
      else
        (labels padTop tickLabelTextSize) behind line.padLeft(labels.extent.width)
    } else {
      if (horizontal) line rotated 90 else line
    }

    if (horizontal)
      Align.center(labeledTickAxis, axisTitle padTop textSize / 2).reduce(Above)
    else
      Align.middle(axisTitle padRight textSize / 2, labeledTickAxis).reduce(Beside)
  }


}
