package com.cibo.evilplot.plot.components

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{AxisDescriptor, Bounds, ContinuousAxisDescriptor, DiscreteAxisDescriptor}
import com.cibo.evilplot.plot.Plot
import com.cibo.evilplot.plot.renderers.{GridLineRenderer, TickRenderer}

object Axes {

  val defaultTickCount: Int = 10

  private sealed trait AxisPlotComponent extends PlotComponent {
    final override val repeated: Boolean = true

    val discrete: Boolean
    val tickRenderer: TickRenderer

    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor

    final protected def ticks(descriptor: AxisDescriptor): Seq[Drawable] = descriptor.labels.map(tickRenderer.render)
  }

  private sealed trait ContinuousAxis {
    final val discrete: Boolean = false
    val tickCount: Int
    def bounds(plot: Plot): Bounds
    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor = ContinuousAxisDescriptor(bounds(plot), tickCount, fixed)
  }

  private sealed trait DiscreteAxis {
    final val discrete: Boolean = true
    val labels: Seq[(String, Double)]
    def getDescriptor(plot: Plot, fixed: Boolean): AxisDescriptor = DiscreteAxisDescriptor(labels)
  }

  private sealed trait XAxisPlotComponent extends AxisPlotComponent {
    final val position: Position = Position.Bottom
    override def size(plot: Plot): Extent = ticks(getDescriptor(plot, fixed = true)).maxBy(_.extent.height).extent

    def bounds(plot: Plot): Bounds = plot.xbounds

    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.width / descriptor.axisBounds.range
      // Move the tick to the center of the range for discrete axes.
      val offset = (if (discrete) scale / 2 else 0) - descriptor.axisBounds.min * scale
      ticks(descriptor).zip(descriptor.values).map { case (tick, value) =>
        val x = offset + value * scale - tick.extent.width / 2
        if (x <= extent.width) {
          tick.translate(x = x)
        } else EmptyDrawable()
      }.group
    }
  }

  private sealed trait YAxisPlotComponent extends AxisPlotComponent {
    final val position: Position = Position.Left
    override def size(plot: Plot): Extent = ticks(getDescriptor(plot, fixed = true)).maxBy(_.extent.width).extent

    def bounds(plot: Plot): Bounds = plot.ybounds

    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.height / descriptor.axisBounds.range
      val ts = ticks(descriptor)
      val maxWidth = ts.maxBy(_.extent.width).extent.width
      // Move the tick to the center of the range for discrete axes.
      val offset = (if (discrete) scale / 2 else 0) - scale * descriptor.axisBounds.min
      val drawable = ts.zip(descriptor.values).map { case (tick, value) =>
        val y = extent.height - (value * scale + offset) - tick.extent.height / 2.0
        if (y <= extent.height) {
          tick.translate(x = maxWidth - tick.extent.width, y = y)
        } else EmptyDrawable()
      }.group
      drawable.translate(x = extent.width - drawable.extent.width)
    }
  }

  private case class ContinuousXAxisPlotComponent(
    tickCount: Int,
    tickRenderer: TickRenderer
  ) extends XAxisPlotComponent with ContinuousAxis

  private case class DiscreteXAxisPlotComponent(
    labels: Seq[(String, Double)],
    tickRenderer: TickRenderer
  ) extends XAxisPlotComponent with DiscreteAxis

  private case class ContinuousYAxisPlotComponent(
    tickCount: Int,
    tickRenderer: TickRenderer
  ) extends YAxisPlotComponent with ContinuousAxis

  private case class DiscreteYAxisPlotComponent(
    labels: Seq[(String, Double)],
    tickRenderer: TickRenderer
  ) extends YAxisPlotComponent with DiscreteAxis

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
    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.width / descriptor.axisBounds.range
      lines(descriptor, extent).zip(descriptor.values).map { case (line, value) =>
        line.translate(x = (value - descriptor.axisBounds.min) * scale + line.extent.width / 2.0)
      }.group
    }
  }

  private trait YGridComponent extends GridComponent {
    def bounds(plot: Plot): Bounds = plot.ybounds
    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot, fixed = true)
      val scale = extent.height / descriptor.axisBounds.range
      val ls = lines(descriptor, extent)
      val maxWidth = ls.maxBy(_.extent.width).extent.width
      ls.zip(descriptor.values).map { case (line, value) =>
        val y = (value - descriptor.axisBounds.min) * scale + line.extent.height / 2.0
        line.translate(x = maxWidth - line.extent.width, y = extent.height - y)
      }.group
    }
  }

  private case class ContinuousXGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer
  ) extends XGridComponent with ContinuousAxis

  private case class ContinuousYGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer
  ) extends YGridComponent with ContinuousAxis

  trait AxesImplicits {
    protected val plot: Plot

    /** Add an X axis to the plot.
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def xAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: TickRenderer = TickRenderer.xAxisTickRenderer()
    ): Plot = {
      val component = ContinuousXAxisPlotComponent(tickCount, tickRenderer)
      component +: plot.xbounds(component.getDescriptor(plot, plot.xfixed).axisBounds)
    }

    /** Add an X axis to the plot
      * @param labels The labels. The x values are assumed to start at 0 and increment by one for each label.
      */
    def xAxis(labels: Seq[String]): Plot = xAxis(labels, labels.indices.map(_.toDouble))

    /** Add an X axis to the plot.
      * @param labels The labels.
      * @param values The X value for each label.
      */
    def xAxis(labels: Seq[String], values: Seq[Double]): Plot = {
      require(labels.lengthCompare(values.length) == 0)
      val labelsAndValues = labels.zip(values)
      val component = DiscreteXAxisPlotComponent(labelsAndValues, TickRenderer.xAxisTickRenderer(rotateText = 90))
      component +: plot.xbounds(component.getDescriptor(plot, plot.xfixed).axisBounds)
    }

    /** Add a Y axis to the plot.
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def yAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: TickRenderer = TickRenderer.yAxisTickRenderer()
    ): Plot = {
      val component = ContinuousYAxisPlotComponent(tickCount, tickRenderer)
      component +: plot.ybounds(component.getDescriptor(plot, plot.yfixed).axisBounds)
    }

    /** Add a Y axis to the plot.
      * @param labels The label. The y values are assumed to start at 0 and increment by one for each label.
      */
    def yAxis(labels: Seq[String]): Plot = yAxis(labels, labels.indices.map(_.toDouble))

    /** Add a Y axis to the plot.
      * @param labels The labels.
      * @param values The Y value for each label.
      */
    def yAxis(labels: Seq[String], values: Seq[Double]): Plot = {
      require(labels.lengthCompare(values.length) == 0)
      val labelsAndValues = labels.zip(values)
      val component = DiscreteYAxisPlotComponent(labelsAndValues, TickRenderer.yAxisTickRenderer())
      component +: plot.ybounds(component.getDescriptor(plot, plot.yfixed).axisBounds)
    }

    def xGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: GridLineRenderer = GridLineRenderer.xGridLineRenderer()
    ): Plot = {
      val component = ContinuousXGridComponent(lineCount, lineRenderer)
      plot.xbounds(component.getDescriptor(plot, plot.xfixed).axisBounds) :+ component
    }

    def yGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: GridLineRenderer = GridLineRenderer.yGridLineRenderer()
    ): Plot = {
      val component = ContinuousYGridComponent(lineCount, lineRenderer)
      plot.ybounds(component.getDescriptor(plot, plot.yfixed).axisBounds) :+ component
    }
  }
}
