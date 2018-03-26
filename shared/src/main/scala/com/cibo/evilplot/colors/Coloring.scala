package com.cibo.evilplot.colors

import com.cibo.evilplot.geometry.{Disc, Rect, Text}
import com.cibo.evilplot.numeric.{Bounds, ContinuousAxisDescriptor}
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.{LegendContext, LegendStyle}

sealed trait Coloring[A] {
  def legendContext(dataToColor: Seq[A])(implicit theme: Theme): LegendContext
  def apply(dataToColor: Seq[A])(implicit theme: Theme): A => Color
}

trait Categorical[A] extends Coloring[A] { self: Coloring[A] =>
  protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
    implicit theme: Theme): (Seq[A], A => Color)
  def apply(dataToColor: Seq[A])(implicit theme: Theme): A => Color = {
    distinctElemsAndColorFunction(dataToColor)._2
  }

  def legendContext(dataToColor: Seq[A])(implicit theme: Theme): LegendContext = {
    val (distinct, coloring) = distinctElemsAndColorFunction(dataToColor)
    LegendContext(
      elements = distinct.map(v => Disc(theme.elements.pointSize) filled coloring(v)),
      labels = distinct.map(a => Text(a.toString, theme.fonts.legendLabelSize)),
      defaultStyle = LegendStyle.Categorical
    )
  }
}

/**
  * Color a variable of type A using the default color stream for the plot's
  * theme.
  * This method will throw an exception if your plot's color stream does not
  * contain enough colors to satisfactorily color the data.
  **/
final case class ThemedCategorical[A: Ordering]() extends Coloring[A] with Categorical[A] {
  protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
    implicit theme: Theme): (Seq[A], A => Color) = {
    val distinctElems = dataToColor.distinct.sorted
    val colors = theme.colors.stream.take(distinctElems.length).toVector
    require(
      colors.length == distinctElems.length,
      s"The color stream for this plot theme does not have enough colors to color $distinctElems")
    (distinctElems, (a: A) => colors(distinctElems.indexOf(a)))
  }
}

/** Color by mapping items to colors.
  * @param enumerated The categories in the order you want them to appear in the legend.
  * @param function Mapping from category to color.
  **/
final case class CategoricalFromFunction[A](enumerated: Seq[A], function: A => Color)
  extends Coloring[A]
    with Categorical[A] {
  protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
    implicit theme: Theme): (Seq[A], A => Color) = {
    (enumerated, function)
  }
}

final case class CategoricalGradient[A: Ordering](colors: Seq[Color])
  extends Coloring[A]
    with Categorical[A] {
  require(colors.nonEmpty, "Cannot make a gradient out of zero colors.")
  protected def distinctElemsAndColorFunction(dataToColor: Seq[A])(
    implicit theme: Theme): (Seq[A], A => Color) = {
    val distinctElems: Seq[A] = dataToColor.distinct.sorted
    val f = GradientUtils.multiGradient(colors, 0, distinctElems.length - 1)
    (distinctElems, (a: A) => f(distinctElems.indexOf(a).toDouble))
  }
}

final case class ContinuousGradient(colors: Seq[Color],
                                    min: Option[Double] = None,
                                    max: Option[Double] = None)
  extends Coloring[Double] {
  require(colors.nonEmpty, "Cannot make a gradient out of zero colors.")

  def apply(dataToColor: Seq[Double])(implicit theme: Theme): Double => Color = {
    val xmin = min.getOrElse(dataToColor.min)
    val xmax = max.getOrElse(dataToColor.max)
    GradientUtils.multiGradient(colors, xmin, xmax)
  }

  def legendContext(coloringDimension: Seq[Double])(implicit theme: Theme): LegendContext = {
    val bounds = Bounds.get(coloringDimension).get
    val axisDescriptor = ContinuousAxisDescriptor(bounds, 10, fixed = true)
    val coloring = apply(coloringDimension)
    LegendContext(
      elements = axisDescriptor.values.map(v =>
        Rect(theme.fonts.legendLabelSize, theme.fonts.legendLabelSize) filled coloring(v)),
      labels = axisDescriptor.labels.map(l => Text(l, theme.fonts.legendLabelSize)),
      defaultStyle = LegendStyle.Gradient
    )
  }
}
object ContinuousGradient {
  def ofTwo(start: Color,
            end: Color,
            min: Option[Double] = None,
            max: Option[Double] = None): ContinuousGradient =
    ContinuousGradient(Seq(start, end), min, max)
  def ofThree(start: Color,
              middle: Color,
              end: Color,
              min: Option[Double],
              max: Option[Double]): ContinuousGradient =
    ContinuousGradient(Seq(start, middle, end), min, max)
}
