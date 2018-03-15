package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, Extent}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PlotRenderer, SurfaceRenderer}

object SurfacePlot {
  private[plot] case class SurfacePlotRenderer(
    data: Seq[Seq[Seq[Point3]]],
    surfaceRenderer: SurfaceRenderer
  ) extends PlotRenderer {

    override def legendContext: LegendContext = surfaceRenderer.legendContext
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)
      data.map { level =>
        val surface = level.map { path =>
          path.withFilter { p =>
            plot.xbounds.isInBounds(p.x) && plot.ybounds.isInBounds(p.y)
          }.map(p => Point3(xtransformer(p.x), ytransformer(p.y), p.z))
        }
        surfaceRenderer.render(plot, plotExtent, surface)
      }.group
    }
  }
}

object ContourPlot {
  import SurfacePlot._
  val defaultGridDimensions: (Int, Int) = (100, 100)

  def apply(data: Seq[Point])(implicit theme: Theme): Plot = {
    val surfaceRenderer = (ps: Seq[Seq[Seq[Point3]]]) => SurfaceRenderer.densityColorContours(ps)
    apply(data, surfaceRenderer)
  }

  def apply(
    data: Seq[Point],
    surfaceRenderer: Seq[Seq[Seq[Point3]]] => SurfaceRenderer,
    gridDimensions: (Int, Int) = defaultGridDimensions,
    contours: Option[Int] = None,
    boundBuffer: Option[Double] = None
  )(implicit theme: Theme): Plot = {

    val contourCount = contours.getOrElse(theme.elements.contours)
    require(contourCount > 0, "Must use at least one contour.")

    val xbounds = Plot.expandBounds(
      Bounds(data.minBy(_.x).x, data.maxBy(_.x).x),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )
    val ybounds = Plot.expandBounds(
      Bounds(data.minBy(_.y).y, data.maxBy(_.y).y),
      boundBuffer.getOrElse(theme.elements.boundBuffer)
    )

    val gridData = KernelDensityEstimation.densityEstimate2D(data, gridDimensions, Some(xbounds), Some(ybounds))

    val binWidth = gridData.zBounds.range / contourCount
    val levels = Seq.tabulate(contourCount - 1) { bin =>
      gridData.zBounds.min + (bin + 1) * binWidth
    }

    val contourPaths = MarchingSquares(levels, gridData).zip(levels).map {
      case (levelPaths, level) => levelPaths.map { path =>
        path.map { case Point(x, y) =>
          Point3(x, y, level)
        }
      }
    }

    val sr = surfaceRenderer(contourPaths)
    Plot(
      xbounds,
      ybounds,
      SurfacePlotRenderer(contourPaths, sr)
    )
  }
}
