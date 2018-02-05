package com.cibo.evilplot.plot

import com.cibo.evilplot.colors.HTMLNamedColors
import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.{AxisDescriptor, ContinuousAxisDescriptor, DiscreteAxisDescriptor}

object Axes {

  val defaultTickCount: Int = 10
  val defaultTickThickness: Double = 1
  val defaultTickLength: Double = 5

  /** Function to render a tick on the x axis.
    * @param length The length of the tick line.
    * @param thickness The thickness of the tick line.
    * @param rotateText The rotation of the label.
    */
  def xAxisTickRenderer(
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness,
    rotateText: Double = 0
  )(label: String): Drawable = {
    val line = Line(length, thickness).rotated(90)
    Align.center(line, Text(label.toString).rotated(rotateText).padTop(2)).reduce(above)
  }

  /** Function to render a tick on the y axis.
    * @param length The length of the tick line.
    * @param thickness The thickness of the tick line.
    */
  def yAxisTickRenderer(
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness
  )(label: String): Drawable = {
    val line = Line(length, thickness)
    Align.middle(Text(label.toString).padRight(2).padBottom(2), line).reduce(beside)
  }

  def xGridLineRenderer(
    thickness: Double = defaultTickThickness
  )(label: String, extent: Extent): Drawable = {
    Line(extent.height, thickness).colored(HTMLNamedColors.white).rotated(90)
  }

  def yGridLineRenderer(
    thickness: Double = defaultTickThickness
  )(label: String, extent: Extent): Drawable = {
    Line(extent.width, thickness).colored(HTMLNamedColors.white)
  }

  private sealed trait AxisPlotComponent extends PlotComponent with Plot.Transformer {
    final override val repeated: Boolean = true

    val tickRenderer: String => Drawable

    def getDescriptor[T](plot: Plot[T]): AxisDescriptor

    final protected def ticks(descriptor: AxisDescriptor): Seq[Drawable] = descriptor.labels.map(tickRenderer)
  }

  private sealed trait ContinuousAxis {
    val tickCount: Int
    def getDescriptor[T](plot: Plot[T]): AxisDescriptor = ContinuousAxisDescriptor(plot.xbounds, tickCount)
  }

  private sealed trait DiscreteAxis {
    val labels: Seq[String]
    def getDescriptor[T](plot: Plot[T]): AxisDescriptor = DiscreteAxisDescriptor(labels)
  }

  private sealed trait XAxisPlotComponent extends AxisPlotComponent {
    final val position: PlotComponent.Position = PlotComponent.Bottom
    override def size[T](plot: Plot[T]): Extent = ticks(getDescriptor(plot)).maxBy(_.extent.height).extent

    def render[T](plot: Plot[T], extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.width / descriptor.axisBounds.range
      ticks(descriptor).zipWithIndex.map { case (tick, i) =>
        val offset = i * descriptor.spacing * scale - tick.extent.width / 2.0
        Translate(tick, x = offset)
      }.group
    }

    def apply(plot: Plot[_], plotExtent: Extent): Double => Double = {
      val descriptor = getDescriptor(plot)
      val scale = plotExtent.width / descriptor.axisBounds.range
      (x: Double) => (x - descriptor.axisBounds.min) * scale
    }
  }

  private sealed trait YAxisPlotComponent extends AxisPlotComponent {
    final val position: PlotComponent.Position = PlotComponent.Left
    override def size[T](plot: Plot[T]): Extent = ticks(getDescriptor(plot)).maxBy(_.extent.width).extent

    def render[T](plot: Plot[T], extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.height / descriptor.axisBounds.range
      val ts = ticks(descriptor)
      val maxWidth = ts.maxBy(_.extent.width).extent.width
      ts.zipWithIndex.map { case (tick, i) =>
        val offset = i * descriptor.spacing * scale + tick.extent.height / 2.0
        Translate(tick, x = maxWidth - tick.extent.width, y = extent.height - offset)
      }.group
    }

    def apply(plot: Plot[_], plotExtent: Extent): Double => Double = {
      val descriptor = getDescriptor(plot)
      val scale = plotExtent.height / descriptor.axisBounds.range
      (y: Double) => plotExtent.height - (y - descriptor.axisBounds.min) * scale
    }
  }

  private case class ContinuousXAxisPlotComponent(
    tickCount: Int,
    tickRenderer: String => Drawable
  ) extends XAxisPlotComponent with ContinuousAxis

  private case class DiscreteXAxisPlotComponent(
    labels: Seq[String],
    tickRenderer: String => Drawable
  ) extends XAxisPlotComponent with DiscreteAxis

  private case class ContinuousYAxisPlotComponent(
    tickCount: Int,
    tickRenderer: String => Drawable
  ) extends YAxisPlotComponent with ContinuousAxis

  private case class DiscreteYAxisPlotComponent(
    labels: Seq[String],
    tickRenderer: String => Drawable
  ) extends YAxisPlotComponent with DiscreteAxis

  private sealed trait GridComponent extends PlotComponent {
    val lineRenderer: (String, Extent) => Drawable
    def getDescriptor[T](plot: Plot[T]): AxisDescriptor

    final val position: PlotComponent.Position = PlotComponent.Background
    override final val repeated: Boolean = true

    protected def lines[T](descriptor: AxisDescriptor, extent: Extent): Seq[Drawable] =
      descriptor.labels.map(l => lineRenderer(l, extent))
  }

  private trait XGridComponent extends GridComponent {
    def render[T](plot: Plot[T], extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.width / descriptor.axisBounds.range
      lines(descriptor, extent).zipWithIndex.map { case (line, i) =>
        val offset = i * descriptor.spacing * scale - line.extent.width / 2.0
        Translate(line, x = offset)
      }.group
    }
  }

  private trait YGridComponent extends GridComponent {
    def render[T](plot: Plot[T], extent: Extent): Drawable = {
      val descriptor = getDescriptor(plot)
      val scale = extent.height / descriptor.axisBounds.range
      val ls = lines(descriptor, extent)
      val maxWidth = ls.maxBy(_.extent.width).extent.width
      ls.zipWithIndex.map { case (line, i) =>
        val offset = i * descriptor.spacing * scale + line.extent.height / 2.0
        Translate(line, x = maxWidth - line.extent.width, y = extent.height - offset)
      }.group
    }
  }

  private case class ContinuousXGridComponent(
    tickCount: Int,
    lineRenderer: (String, Extent) => Drawable
  ) extends XGridComponent with ContinuousAxis

  private case class DiscreteXGridComponent(
    labels: Seq[String],
    lineRenderer: (String, Extent) => Drawable
  ) extends XGridComponent with DiscreteAxis

  private case class ContinuousYGridComponent(
    tickCount: Int,
    lineRenderer: (String, Extent) => Drawable
  ) extends YGridComponent with ContinuousAxis

  private case class DiscreteYGridComponent(
    labels: Seq[String],
    lineRenderer: (String, Extent) => Drawable
  ) extends YGridComponent with DiscreteAxis

  trait AxesImplicits[T] {
    protected val plot: Plot[T]

    /** Add an X axis to the plot.
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def xAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: String => Drawable = xAxisTickRenderer()
    ): Plot[T] = {
      val component = ContinuousXAxisPlotComponent(tickCount, tickRenderer)
      component +: plot.copy(xtransform = component)
    }

    /** Add a Y axis to the plot.
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def yAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: String => Drawable = yAxisTickRenderer()
    ): Plot[T] = {
      val component = ContinuousYAxisPlotComponent(tickCount, tickRenderer)
      component +: plot.copy(ytransform = component)
    }

    def xGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: (String, Extent) => Drawable = xGridLineRenderer()
    ): Plot[T] = {
      plot :+ ContinuousXGridComponent(lineCount, lineRenderer)
    }

    def yGrid(
      lineCount: Int = defaultTickCount,
      lineRenderer: (String, Extent) => Drawable = yGridLineRenderer()
    ): Plot[T] = {
      plot :+ ContinuousYGridComponent(lineCount, lineRenderer)
    }
  }
}
