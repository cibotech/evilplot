package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent}
import com.cibo.evilplot.numeric._
import com.cibo.evilplot.plot.aesthetics.Theme
import com.cibo.evilplot.plot.renderers.{PathRenderer, PlotRenderer, SurfaceRenderer}

object SurfacePlot {
  private[plot] val defaultBoundBuffer: Double = 0.2

  private[plot] case class SurfacePlotRenderer(
    data: Seq[Seq[Point3]],
    surfaceRenderer: SurfaceRenderer
  ) extends PlotRenderer {
    override def legendContext: LegendContext = surfaceRenderer.legendContext
    def render(plot: Plot, plotExtent: Extent)(implicit theme: Theme): Drawable = {
      val xtransformer = plot.xtransform(plot, plotExtent)
      val ytransformer = plot.ytransform(plot, plotExtent)

      data.map { level =>
        val transformedLevel = level.withFilter { p =>
          plot.xbounds.isInBounds(p.x) && plot.ybounds.isInBounds(p.y)
        }.map { p => Point3(xtransformer(p.x), ytransformer(p.y), p.z) }
        val path = surfaceRenderer.render(plot, plotExtent, transformedLevel)
        if (path.extent.width < 1.0 && path.extent.height < 1.0) EmptyDrawable()
        else path
      }.group
    }
  }
}

object ContourPlot {
  import SurfacePlot._
  val defaultNumContours: Int = 20
  val defaultGridDimensions: (Int, Int) = (100, 100)

  def apply(data: Seq[Point])(implicit theme: Theme): Plot = {
    val surfaceRenderer = SurfaceRenderer.densityColorContours(theme)(_)
    apply(data, surfaceRenderer)
  }

  def apply(
    data: Seq[Point],
    surfaceRenderer: Seq[Seq[Point3]] => SurfaceRenderer,
    gridDimensions: (Int, Int) = defaultGridDimensions,
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

    val contourPoints = levels.map { l =>
      toPoint3(MarchingSquares.getContoursAt(l, gridData), l)
    }

    val sr = surfaceRenderer(contourPoints)
    Plot(
      xbounds,
      ybounds,
      SurfacePlotRenderer(contourPoints, sr)
    )
  }

  // MS implementation returns Seq[Segment], bridge to Seq[Point3] to avoid
  // breaking old plots (for now...)
  private def toPoint3(segments: Seq[Segment],
                       level: Double): Vector[Point3] = {
    segments
      .flatMap(s =>
        Vector(Point3(s.a.x, s.a.y, level), Point3(s.b.x, s.b.y, level)))
      .toVector
  }
}
