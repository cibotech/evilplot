package com.cibo.evilplot.plot2d

import com.cibo.evilplot.geometry._
import com.cibo.evilplot.numeric.AxisDescriptor
import com.cibo.evilplot.plot.Chart

object Axes {

  val defaultTickCount: Int = 10
  val defaultTickThickness: Double = 1
  val defaultTickLength: Double = 5

  /** Function to render a tick on the x axis.
    * @param length The length of the tick line.
    * @param thickness The thickness of the tick line.
    * @param rotateText The rotation of the label.
    */
  def xAxisTickRenderer[X](
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness,
    rotateText: Double = 0
  )(labelOpt: Option[X]): Drawable = {
    val line = Line(length, thickness).rotated(90)
    labelOpt match {
      case Some(label) => Align.center(line, Text(label.toString).rotated(rotateText).padTop(2)).reduce(above)
      case None        => line
    }
  }

  /** Function to render a tick on the y axis.
    * @param length The length of the tick line.
    * @param thickness The thickness of the tick line.
    */
  def yAxisTickRenderer[Y](
    length: Double = defaultTickLength,
    thickness: Double = defaultTickThickness
  )(labelOpt: Option[Y]): Drawable = {
    val line = Line(length, thickness)
    labelOpt match {
      case Some(label) => Align.middle(Text(label.toString).padRight(2).padBottom(2), line).reduce(beside)
      case None        => line
    }
  }

  private case class XAxisLabelAnnotation[T](label: Drawable) extends PlotAnnotation[T] {
    val position: PlotAnnotation.Position = PlotAnnotation.Bottom
    def size(plot: Plot2D[T]): Extent = label.extent
    def render(plot: Plot2D[T], extent: Extent): Drawable = label.center(extent.width)
  }

  private case class YAxisLabelAnnotation[T](label: Drawable) extends PlotAnnotation[T] {
    val position: PlotAnnotation.Position = PlotAnnotation.Left
    def size(plot: Plot2D[T]): Extent = label.extent
    def render(plot: Plot2D[T], extent: Extent): Drawable = label.middle(extent.height)
  }

  private abstract class AxisAnnotation[T] extends PlotAnnotation[T] with Plot2D.Transformer[T] {
    val tickCount: Int
    val tickRenderer: Option[String] => Drawable

    protected def ticks(descriptor: AxisDescriptor): Seq[Drawable] = {
      for {
        i <- 0 until descriptor.numTicks
        x = descriptor.axisBounds.min + i * descriptor.spacing
        label = Chart.createNumericLabel(x, descriptor.numFrac)
      } yield tickRenderer(Some(label))
    }
  }

  private case class XAxisAnnotation[T](
    tickCount: Int,
    tickRenderer: Option[String] => Drawable
  ) extends AxisAnnotation[T] {
    val position: PlotAnnotation.Position = PlotAnnotation.Bottom

    def size(plot: Plot2D[T]): Extent =
      ticks(AxisDescriptor(plot.xbounds, tickCount)).maxBy(_.extent.height).extent

    def render(plot: Plot2D[T], extent: Extent): Drawable = {
      val descriptor = AxisDescriptor(plot.xbounds, tickCount)
      val scale = extent.width / descriptor.axisBounds.range
      ticks(descriptor).zipWithIndex.map { case (tick, i) =>
        val offset = i * descriptor.spacing * scale - tick.extent.width / 2.0
        Translate(tick, x = offset)
      }.group
    }

    def apply(plot: Plot2D[T], extent: Extent): Double => Double = {
      val descriptor = AxisDescriptor(plot.xbounds, tickCount)
      val scale = extent.width / descriptor.axisBounds.range
      (x: Double) => (x - descriptor.axisBounds.min) * scale
    }
  }

  private case class YAxisAnnotation[T](
    tickCount: Int,
    tickRenderer: Option[String] => Drawable
  ) extends AxisAnnotation[T] {
    val position: PlotAnnotation.Position = PlotAnnotation.Left

    def size(plot: Plot2D[T]): Extent =
      ticks(AxisDescriptor(plot.ybounds, tickCount)).maxBy(_.extent.width).extent

    def render(plot: Plot2D[T], extent: Extent): Drawable = {
      val descriptor = AxisDescriptor(plot.ybounds, tickCount)
      val scale = extent.height / descriptor.axisBounds.range
      val ts = ticks(descriptor)
      val maxWidth = ts.maxBy(_.extent.width).extent.width
      ts.zipWithIndex.map { case (tick, i) =>
        val offset = i * descriptor.spacing * scale + tick.extent.height / 2.0
        Translate(tick, x = maxWidth - tick.extent.width, y = extent.height - offset)
      }.group
    }

    def apply(plot: Plot2D[T], extent: Extent): Double => Double = {
      val descriptor = AxisDescriptor(plot.ybounds, tickCount)
      val scale = extent.height / descriptor.axisBounds.range
      (y: Double) => extent.height - (y - descriptor.axisBounds.min) * scale
    }
  }

  private case class TitleAnnotation[T](d: Drawable) extends PlotAnnotation[T] {
    val position: PlotAnnotation.Position = PlotAnnotation.Top
    def size(plot: Plot2D[T]): Extent = d.extent
    def render(plot: Plot2D[T], extent: Extent): Drawable = d.center(extent.width)
  }

  implicit class Plot2DAxes[T](plot: Plot2D[T]) {

    def title(d: Drawable): Plot2D[T] = plot.copy(annotations = plot.annotations :+ TitleAnnotation[T](d))
    def title(label: String, size: Double = 22): Plot2D[T] = title(Text(label, size))

    def xLabel(d: Drawable): Plot2D[T] = plot.copy(annotations = plot.annotations :+ XAxisLabelAnnotation[T](d))
    def xLabel(label: String, size: Double = 20): Plot2D[T] = xLabel(Text(label, size).padTop(4))

    def yLabel(d: Drawable): Plot2D[T] = plot.copy(annotations = plot.annotations :+ YAxisLabelAnnotation[T](d))
    def yLabel(label: String, size: Double = 20): Plot2D[T] = yLabel(Text(label, size).rotated(270).padLeft(4))

    /** Add an X axis to the plot.
      *
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def xAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: Option[String] => Drawable = xAxisTickRenderer()
    ): Plot2D[T] = {
      val annotation = XAxisAnnotation[T](tickCount, tickRenderer)
      plot.copy(
        annotations = annotation +: plot.annotations,
        xtransform = annotation
      )
    }

    /** Add a Y axis to the plot.
      *
      * @param tickCount    The number of tick lines.
      * @param tickRenderer Function to draw a tick line/label.
      */
    def yAxis(
      tickCount: Int = defaultTickCount,
      tickRenderer: Option[String] => Drawable = yAxisTickRenderer()
    ): Plot2D[T] = {
      val annotation = YAxisAnnotation[T](tickCount, tickRenderer)
      plot.copy(
        annotations = annotation +: plot.annotations,
        ytransform = annotation
      )
    }
  }
}
