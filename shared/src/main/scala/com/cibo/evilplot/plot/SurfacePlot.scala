package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, SurfaceRenderer}

object SurfacePlot {
  private[plot] val defaultBoundBuffer: Double = 0.2

  private[plot] case class SurfacePlotRenderer(
    data: Seq[Seq[Seq[Point3]]],
    surfaceRenderer: SurfaceRenderer
  ) extends PlotRenderer {
    def render(plot: Plot, plotExtent: Extent): Drawable = {
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
  val defaultNumContours: Int = 20
  val defaultGridDimensions: (Int, Int) = (100, 100)

  def apply(
    data: Seq[Point],
    gridDimensions: (Int, Int) = defaultGridDimensions,
    surfaceRenderer: Seq[Seq[Seq[Point3]]] => SurfaceRenderer = SurfaceRenderer.densityColorContours(),
    contours: Int = defaultNumContours,
    boundBuffer: Double = defaultBoundBuffer
  ): Plot = {
    require(contours > 0, "Must use at least one contour.")

    val xbounds = Plot.expandBounds(Bounds(data.minBy(_.x).x, data.maxBy(_.x).x), boundBuffer)
    val ybounds = Plot.expandBounds(Bounds(data.minBy(_.y).y, data.maxBy(_.y).y), boundBuffer)

    val gridData = KernelDensityEstimation.densityEstimate2D(data, gridDimensions, Some(xbounds), Some(ybounds))

    val binWidth = gridData.zBounds.range / contours
    val levels = Seq.tabulate(contours - 1) { bin =>
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
