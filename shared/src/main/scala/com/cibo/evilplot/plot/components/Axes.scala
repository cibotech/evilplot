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

package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{GridLineRenderer, TickRenderer}

object Axes {

  private sealed trait AxisPlotComponent extends PlotComponent {
    final override val repeated: Boolean = true

    val tickRenderer: TickRenderer

    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor

    final protected def ticks(descriptor: AxisDescriptor): Seq[Drawable] =
      descriptor.labels.map(tickRenderer.render)
  }

  private sealed trait ContinuousAxis {
    val tickCount: Int
    val tickCountRange: Option[Seq[Int]]
    val labelFormatter: Option[Double => String] = None
    def bounds(plot: Plot): Bounds
    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor =
      Labeling.label(
        bounds(plot),
        preferredTickCount = Some(tickCount),
        tickCountRange = tickCountRange,
        formatter = labelFormatter,
        fixed = fixed)
  }

  private sealed trait DiscreteAxis {
    val labels: Seq[(String, Double)]
    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor = DiscreteAxisDescriptor(labels)
  }

  private sealed trait ArbitraryAxisPlotComponent extends AxisPlotComponent {
    val align: Double = 0

    override def size(plot: Plot): Extent = {
      val extents = ticks(getDescriptor(plot, fixed = true)).map(_.extent)
      position match {
        case Position.Left | Position.Right => extents.maxBy(_.width)
        case Position.Bottom | Position.Top => extents.maxBy(_.height)
        case _                              => Extent(extents.maxBy(_.width).width, extents.maxBy(_.height).height)
      }
    }

    def bounds(plot: Plot): Bounds

    def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = position match {
        case Position.Left | Position.Right => extent.height / descriptor.axisBounds.range
        case Position.Bottom | Position.Top => extent.width / descriptor.axisBounds.range
        case _                              => 1 //TODO replace with scaling when available
      }
      val ts = ticks(descriptor)
      val maxWidth = ts.maxBy(_.extent.width).extent.width
      val maxHeight = ts.maxBy(_.extent.height).extent.height
      // Move the tick to a position within the range for discrete axes.
      val offset = scale * align - scale * descriptor.axisBounds.min
      position match {
        case Position.Left | Position.Right =>
          val drawable = ts
            .zip(descriptor.values)
            .map {
              case (tick, value) =>
                val y = extent.height - (value * scale + offset) - tick.extent.height / 2.0
                if (y <= extent.height) {
                  position match {
                    case Position.Left => tick.translate(x = maxWidth - tick.extent.width, y = y)
                    case _             => tick.translate(y = y)
                  }
                } else EmptyDrawable()
            }
            .group
          drawable.translate(x = extent.width - drawable.extent.width)
        case Position.Bottom | Position.Top =>
          ts.zip(descriptor.values)
            .map {
              case (tick, value) =>
                val x = offset + value * scale - tick.extent.width / 2
                if (x <= extent.width) {
                  position match {
                    case Position.Top => tick.translate(x = x, y = maxHeight - tick.extent.height)
                    case _            => tick.translate(x = x)
                  }
                } else EmptyDrawable()
            }
            .group
        case _ => ts.group
      }
    }
  }

  private case class ContinuousAxisPlotComponent(
    boundsFn: Plot => Bounds,
    override val position: Position,
    tickCount: Int,
    tickRenderer: TickRenderer,
    override val labelFormatter: Option[Double => String],
    tickCountRange: Option[Seq[Int]]
  ) extends ArbitraryAxisPlotComponent
      with ContinuousAxis {
    override def bounds(plot: Plot): Bounds = boundsFn(plot)
  }

  private case class DiscreteAxisPlotComponent(
    boundsFn: Plot => Bounds,
    override val position: Position,
    labels: Seq[(String, Double)],
    tickRenderer: TickRenderer,
    override val align: Double
  ) extends ArbitraryAxisPlotComponent
      with DiscreteAxis {
    override def bounds(plot: Plot): Bounds = boundsFn(plot)
  }

  private sealed trait GridComponent extends PlotComponent {
    val lineRenderer: GridLineRenderer
    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor

    final val position: Position = Position.Background
    override final val repeated: Boolean = true

    protected def lines(descriptor: AxisDescriptor, extent: Extent): Seq[Drawable] =
      descriptor.labels.map(l => lineRenderer.render(extent, l))
  }

  private trait XGridComponent extends GridComponent {
    def bounds(plot: Plot): Bounds = plot.xbounds
    def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.width / descriptor.axisBounds.range
      lines(descriptor, extent)
        .zip(descriptor.values)
        .map {
          case (line, value) =>
            line.translate(
              x = (value - descriptor.axisBounds.min) * scale - line.extent.width / 2.0)
        }
        .group
    }
  }

  private trait YGridComponent extends GridComponent {
    def bounds(plot: Plot): Bounds = plot.ybounds
    def render(plot: Plot, extent: Extent)(implicit theme: Theme): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.height / descriptor.axisBounds.range
      val ls = lines(descriptor, extent)
      val maxWidth = ls.maxBy(_.extent.width).extent.width
      ls.zip(descriptor.values)
        .map {
          case (line, value) =>
            val y = (value - descriptor.axisBounds.min) * scale + line.extent.height / 2.0
            line.translate(x = maxWidth - line.extent.width, y = extent.height - y)
        }
        .group
    }
  }

  private case class ContinuousXGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer,
    tickCountRange: Option[Seq[Int]]
  ) extends XGridComponent
      with ContinuousAxis

  private case class ContinuousYGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer,
    tickCountRange: Option[Seq[Int]]
  ) extends YGridComponent
      with ContinuousAxis

  trait AxesImplicits {
    protected val plot: Plot

    /** Add a continuous axis to the plot.
      * @param boundsFn         Takes a plot and returns the bounds this axis will display.
      * @param position         The side of the plot to add the axis.
      * @param tickCount        The number of tick lines.
      * @param tickRenderer     Function to draw a tick line/label.
      * @param labelFormatter   Custom function to format tick labels.
      * @param tickCountRange   Allow searching over axis labels with this many ticks.
      * @param updatePlotBounds Set plot bounds to match the axis bounds.
      */
    def continuousAxis(
      boundsFn: Plot => Bounds,
      position: Position,
      tickCount: Option[Int] = None,
      tickRenderer: Option[TickRenderer] = None,
      labelFormatter: Option[Double => String] = None,
      tickCountRange: Option[Seq[Int]] = None,
      updatePlotBounds: Boolean = true
    )(implicit theme: Theme): Plot = {
      val defaultRotation = if (position == Position.Bottom || position == Position.Top) {
        theme.elements.continuousXAxisLabelOrientation
      } else {
        theme.elements.continuousYAxisLabelOrientation
      }
      val component = ContinuousAxisPlotComponent(
        boundsFn,
        position,
        tickCount.getOrElse(theme.elements.tickCount),
        tickRenderer.getOrElse(
          TickRenderer.axisTickRenderer(
            position,
            theme.elements.tickLength,
            theme.elements.tickThickness,
            defaultRotation
          )),
        labelFormatter,
        tickCountRange
      )
      if (updatePlotBounds) {
        position match {
          case Position.Left | Position.Right =>
            component +: plot.ybounds(component.getDescriptor(plot, plot.yfixed).axisBounds)
          case Position.Bottom | Position.Top =>
            component +: plot.xbounds(component.getDescriptor(plot, plot.xfixed).axisBounds)
          case _ =>
            component +: plot
        }
      } else {
        component +: plot //TODO make prepending component optional / exposed?
      }
    }

    /** Add a discrete axis to the plot.
      * @param labels           The labels.
      * @param values           The X value for each label.
      * @param position         The side of the plot to add the axis.
      * @param updatePlotBounds Set plot bounds to match the axis bounds.
      * @param tickRenderer     Function to draw a tick line/label.
      * @param align            Where to align ticks as a proportion of their band, e.g. 0 = left, 0.5 = center.
      */
    def discreteAxis(
      labels: Seq[String],
      values: Seq[Double],
      position: Position,
      updatePlotBounds: Boolean = true,
      tickRenderer: Option[TickRenderer] = None,
      align: Double = 0.5
    )(implicit theme: Theme): Plot = {
      require(labels.lengthCompare(values.length) == 0)
      require(0.0 <= align && align <= 1.0, "discreteAxis requires an align value from 0 to 1")
      val labelsAndValues = labels.zip(values)
      val (boundsFn, defaultRotation) =
        if (position == Position.Bottom || position == Position.Top) {
          ((plot: Plot) => plot.xbounds, theme.elements.categoricalXAxisLabelOrientation)
        } else {
          ((plot: Plot) => plot.ybounds, theme.elements.categoricalYAxisLabelOrientation)
        }
      val component = DiscreteAxisPlotComponent(
        boundsFn,
        position,
        labelsAndValues,
        tickRenderer.getOrElse(
          TickRenderer.axisTickRenderer(
            position,
            theme.elements.tickLength,
            theme.elements.tickThickness,
            defaultRotation
          )),
        align
      )
      if (updatePlotBounds) {
        position match {
          case Position.Left | Position.Right =>
            component +: plot.ybounds(component.getDescriptor(plot, true).axisBounds)
          case Position.Bottom | Position.Top =>
            component +: plot.xbounds(component.getDescriptor(plot, true).axisBounds)
          case _ =>
            component +: plot
        }
      } else {
        component +: plot
      }
    }

    /** Add an X axis to the plot.
      * @param tickCount      The number of tick lines.
      * @param tickRenderer   Function to draw a tick line/label.
      * @param labelFormatter Custom function to format tick labels.
      * @param tickCountRange Allow searching over axis labels with this many ticks.
      * @param position       The side of the plot to add the axis.
      */
    def xAxis(
      tickCount: Option[Int] = None,
      tickRenderer: Option[TickRenderer] = None,
      labelFormatter: Option[Double => String] = None,
      tickCountRange: Option[Seq[Int]] = None,
      position: Position = Position.Bottom
    )(implicit theme: Theme): Plot = {
      require(
        position == Position.Bottom || position == Position.Top,
        "xAxis expects Position.Bottom or Position.Top.")
      continuousAxis(
        p => p.xbounds,
        position,
        Some(tickCount.getOrElse(theme.elements.xTickCount)),
        tickRenderer,
        labelFormatter,
        tickCountRange,
        true
      )
    }

    /** Add an X axis to the plot
      * @param labels The labels. The x values are assumed to start at 0 and increment by one for each label.
      */
    def xAxis(labels: Seq[String])(implicit theme: Theme): Plot =
      xAxis(labels, labels.indices.map(_.toDouble))

    /** Add an X axis to the plot
      * @param labels   The labels. The x values are assumed to start at 0 and increment by one for each label.
      * @param position The side of the plot to add the axis.
      */
    def xAxis(
      labels: Seq[String],
      position: Position
    )(implicit theme: Theme): Plot =
      xAxis(labels, labels.indices.map(_.toDouble), position)

    /** Add an X axis to the plot.
      * @param labels The labels.
      * @param values The X value for each label.
      */
    def xAxis(
      labels: Seq[String],
      values: Seq[Double]
    )(implicit theme: Theme): Plot = {
      xAxis(labels, values, Position.Bottom)
    }

    /** Add an X axis to the plot.
      * @param labels   The labels.
      * @param values   The X value for each label.
      * @param position The side of the plot to add the axis.
      */
    def xAxis(
      labels: Seq[String],
      values: Seq[Double],
      position: Position
    )(implicit theme: Theme): Plot = {
      require(
        position == Position.Bottom || position == Position.Top,
        "xAxis expects Position.Bottom or Position.Top.")
      discreteAxis(labels, values, position, true)
    }

    /** Add a Y axis to the plot.
      * @param tickCount      The number of tick lines.
      * @param tickRenderer   Function to draw a tick line/label.
      * @param labelFormatter Custom function to format tick labels.
      * @param tickCountRange Allow searching over axis labels with this many ticks.
      * @param position       The side of the plot to add the axis.
      */
    def yAxis(
      tickCount: Option[Int] = None,
      tickRenderer: Option[TickRenderer] = None,
      labelFormatter: Option[Double => String] = None,
      tickCountRange: Option[Seq[Int]] = None,
      position: Position = Position.Left
    )(implicit theme: Theme): Plot = {
      require(
        position == Position.Left || position == Position.Right,
        "yAxis expects Position.Left or Position.Right.")
      continuousAxis(
        p => p.ybounds,
        position,
        Some(tickCount.getOrElse(theme.elements.yTickCount)),
        tickRenderer,
        labelFormatter,
        tickCountRange,
        true
      )
    }

    /** Add a Y axis to the plot.
      * @param labels The label. The y values are assumed to start at 0 and increment by one for each label.
      */
    def yAxis(labels: Seq[String])(implicit theme: Theme): Plot =
      yAxis(labels, labels.indices.map(_.toDouble))

    /** Add a Y axis to the plot.
      * @param labels   The label. The y values are assumed to start at 0 and increment by one for each label.
      * @param position The side of the plot to add the axis.
      */
    def yAxis(
      labels: Seq[String],
      position: Position
    )(implicit theme: Theme): Plot =
      yAxis(labels, labels.indices.map(_.toDouble), position)

    /** Add a Y axis to the plot.
      * @param labels The labels.
      * @param values The Y value for each label.
      */
    def yAxis(
      labels: Seq[String],
      values: Seq[Double]
    )(implicit theme: Theme): Plot = {
      yAxis(labels, values, Position.Left)
    }

    /** Add a Y axis to the plot.
      * @param labels   The labels.
      * @param values   The Y value for each label.
      * @param position The side of the plot to add the axis.
      */
    def yAxis(
      labels: Seq[String],
      values: Seq[Double],
      position: Position
    )(implicit theme: Theme): Plot = {
      require(
        position == Position.Left || position == Position.Right,
        "yAxis expects Position.Left or Position.Right.")
      discreteAxis(labels, values, position, true)
    }

    /** Add x grid lines to the plot.
      * @param lineCount the number of grid lines to use
      * @param lineRenderer the grid line renderer
      */
    def xGrid(
      lineCount: Option[Int] = None,
      lineRenderer: Option[GridLineRenderer] = None,
      tickCountRange: Option[Seq[Int]] = None
    )(implicit theme: Theme): Plot = {
      val component = ContinuousXGridComponent(
        lineCount.getOrElse(theme.elements.xGridLineCount),
        lineRenderer.getOrElse(GridLineRenderer.xGridLineRenderer()),
        tickCountRange
      )
      plot.xbounds(component.getDescriptor(plot, plot.xfixed).axisBounds) :+ component
    }

    /** Add y grid lines to the plot.
      * @param lineCount the number of grid lines to use
      * @param lineRenderer the grid line renderer
      */
    def yGrid(
      lineCount: Option[Int] = None,
      lineRenderer: Option[GridLineRenderer] = None,
      tickCountRange: Option[Seq[Int]] = None
    )(implicit theme: Theme): Plot = {
      val component = ContinuousYGridComponent(
        lineCount.getOrElse(theme.elements.yGridLineCount),
        lineRenderer.getOrElse(GridLineRenderer.yGridLineRenderer()),
        tickCountRange
      )
      plot.ybounds(component.getDescriptor(plot, plot.yfixed).axisBounds) :+ component
    }
  }
}
