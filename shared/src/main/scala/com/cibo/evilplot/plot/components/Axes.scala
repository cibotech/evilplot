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

    def getDescriptor(plot: Plot): AxisDescriptor

    final protected def ticks(descriptor: AxisDescriptor): Seq[Drawable] = descriptor.labels.map(tickRenderer.render)
  }

  private sealed trait ContinuousAxis {
    final val discrete: Boolean = false
    val tickCount: Int
    def bounds(plot: Plot): Bounds
    def getDescriptor(plot: Plot): AxisDescriptor = ContinuousAxisDescriptor(bounds(plot), tickCount)
  }

  private sealed trait DiscreteAxis {
    final val discrete: Boolean = true
    val labels: Seq[String]
    def getDescriptor(plot: Plot): AxisDescriptor = DiscreteAxisDescriptor(labels)
  }

  private sealed trait XAxisPlotComponent extends AxisPlotComponent {
    final val position: Position = Position.Bottom
    override def size(plot: Plot): Extent = ticks(getDescriptor(plot)).maxBy(_.extent.height).extent

    def bounds(plot: Plot): Bounds = plot.xbounds

    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.width / descriptor.axisBounds.range
      ticks(descriptor).zipWithIndex.map { case (tick, i) =>
        val offset = if (discrete) descriptor.spacing * scale / 2 else 0
        val x = offset + i * descriptor.spacing * scale - tick.extent.width / 2.0
        tick.translate(x = x)
      }.group
    }
  }

  private sealed trait YAxisPlotComponent extends AxisPlotComponent {
    final val position: Position = Position.Left
    override def size(plot: Plot): Extent = ticks(getDescriptor(plot)).maxBy(_.extent.width).extent

    def bounds(plot: Plot): Bounds = plot.ybounds

    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.height / descriptor.axisBounds.range
      val ts = ticks(descriptor)
      val maxWidth = ts.maxBy(_.extent.width).extent.width
      ts.zipWithIndex.map { case (tick, i) =>
        val offset = if (discrete) descriptor.spacing * scale / 2 else 0
        val y = extent.height - (i * descriptor.spacing * scale + offset) - tick.extent.height / 2.0
        tick.translate(x = maxWidth - tick.extent.width, y = y)
      }.group
    }
  }

  private case class ContinuousXAxisPlotComponent(
    tickCount: Int,
    tickRenderer: TickRenderer
  ) extends XAxisPlotComponent with ContinuousAxis

  private case class DiscreteXAxisPlotComponent(
    labels: Seq[String],
    tickRenderer: TickRenderer
  ) extends XAxisPlotComponent with DiscreteAxis

  private case class ContinuousYAxisPlotComponent(
    tickCount: Int,
    tickRenderer: TickRenderer
  ) extends YAxisPlotComponent with ContinuousAxis

  private case class DiscreteYAxisPlotComponent(
    labels: Seq[String],
    tickRenderer: TickRenderer
  ) extends YAxisPlotComponent with DiscreteAxis

  private sealed trait GridComponent extends PlotComponent {
    val lineRenderer: GridLineRenderer
    def getDescriptor(plot: Plot): AxisDescriptor

    final val position: Position = Position.Background
    override final val repeated: Boolean = true

    protected def lines(descriptor: AxisDescriptor, extent: Extent): Seq[Drawable] =
      descriptor.labels.map(l => lineRenderer.render(extent, l))
  }

  private trait XGridComponent extends GridComponent {
    def bounds(plot: Plot): Bounds = plot.xbounds
    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.width / descriptor.axisBounds.range
      lines(descriptor, extent).zipWithIndex.map { case (line, i) =>
        val offset = i * descriptor.spacing * scale - line.extent.width / 2.0
        line.translate(x = offset)
      }.group
    }
  }

  private trait YGridComponent extends GridComponent {
    def bounds(plot: Plot): Bounds = plot.ybounds
    def render(plot: Plot, extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.height / descriptor.axisBounds.range
      val ls = lines(descriptor, extent)
      val maxWidth = ls.maxBy(_.extent.width).extent.width
      ls.zipWithIndex.map { case (line, i) =>
        val offset = i * descriptor.spacing * scale + line.extent.height / 2.0
        line.translate(x = maxWidth - line.extent.width, y = extent.height - offset)
      }.group
    }
  }

  private case class ContinuousXGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer
  ) extends XGridComponent with ContinuousAxis

  private case class DiscreteXGridComponent(
    labels: Seq[String],
    lineRenderer: GridLineRenderer
  ) extends XGridComponent with DiscreteAxis

  private case class ContinuousYGridComponent(
    tickCount: Int,
    lineRenderer: GridLineRenderer
  ) extends YGridComponent with ContinuousAxis

  private case class DiscreteYGridComponent(
    labels: Seq[String],
    lineRenderer: GridLineRenderer
  ) extends YGridComponent with DiscreteAxis

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
      component +: plot.xbounds(component.getDescriptor(plot).axisBounds)
    }

    def xAxis(labels: Seq[String]): Plot = {
      val component = DiscreteXAxisPlotComponent(labels, TickRenderer.xAxisTickRenderer(rotateText = 90))
      component +: plot.xbounds(component.getDescriptor(plot).axisBounds)
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
      component +: plot.ybounds(component.getDescriptor(plot).axisBounds)
    }

    def yAxis(labels: Seq[String]): Plot = {
      val component = DiscreteYAxisPlotComponent(labels, TickRenderer.yAxisTickRenderer())
      component +: plot.ybounds(component.getDescriptor(plot).axisBounds)
    }

    def xGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: GridLineRenderer = GridLineRenderer.xGridLineRenderer()
    ): Plot = {
      val component = ContinuousXGridComponent(lineCount, lineRenderer)
      plot.xbounds(component.getDescriptor(plot).axisBounds) :+ component
    }

    def yGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: GridLineRenderer = GridLineRenderer.yGridLineRenderer()
    ): Plot = {
      val component = ContinuousYGridComponent(lineCount, lineRenderer)
      plot.ybounds(component.getDescriptor(plot).axisBounds) :+ component
    }
  }
}
