package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.{Drawable, EmptyDrawable, Extent, Group, Translate}

object Facets {

  type FacetData = Seq[Seq[Plot[_]]]

  // Divide the plotExtent evenly among subplots.
  private def computeSubplotExtent(plot: Plot[FacetData], plotExtent: Extent): Extent = {
    val rows = plot.data.size
    val cols = plot.data.map(_.size).max
    Extent(plotExtent.width / cols, plotExtent.height / rows)
  }

  // Add padding to subplots so they are all the same size and use the same axis transformation.
  private def updatePlotsForFacet(plot: Plot[FacetData], subplotExtent: Extent): FacetData = {
    Plot.padPlots(plot.data, subplotExtent).map(_.map(_.setTransform(plot.xtransform, plot.ytransform)))
  }

  private def facetedPlotRenderer(plot: Plot[FacetData], plotExtent: Extent): Drawable = {
    // Make sure all subplots have the same size plot area.
    val innerExtent = computeSubplotExtent(plot, plotExtent)
    val paddedPlots = updatePlotsForFacet(plot, innerExtent)

    // Render the plots.
    paddedPlots.zipWithIndex.map { case (row, yIndex) =>
      val y = yIndex * innerExtent.height
      row.zipWithIndex.map { case (subplot, xIndex) =>
        val x = xIndex * innerExtent.width
        Translate(subplot.render(innerExtent), x = x, y = y)
      }.group
    }.group
  }

  private val empty: Drawable = EmptyDrawable()

  private def topComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent
  ): Drawable = {
    val pextent = subplot.plotExtent(subplotExtent)
    plot.components.filter(_.position == PlotComponent.Top).reverse.foldLeft(empty) { (d, a) =>
      Translate(
        a.render(subplot, pextent),
        x = plot.plotOffset.x + subplot.plotOffset.x,
        y = d.extent.height
      ) behind d
    }
  }

  private def bottomComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent,
    extent: Extent
  ): Drawable = {
    val pextent = subplot.plotExtent(subplotExtent)
    val startY = extent.height
    plot.components.filter { a =>
      a.position == PlotComponent.Bottom
    }.reverse.foldLeft((startY, empty)) { case ((y, d), a) =>
      val rendered = a.render(subplot, pextent)
      val newX = plot.plotOffset.x + subplot.plotOffset.x
      val newY = y - rendered.extent.height
      (newY, Translate(rendered, x = newX, y = newY) behind d)
    }._2
  }

  private def leftComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent
  ): Drawable = {
    val pextent = subplot.plotExtent(subplotExtent)
    plot.components.filter(_.position == PlotComponent.Left).foldLeft(empty) { (d, a) =>
      Translate(a.render(subplot, pextent), y = subplot.plotOffset.y + plot.plotOffset.y) beside d
    }
  }

  private def rightComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent,
    extent: Extent
  ): Drawable = {
    val pextent = subplot.plotExtent(subplotExtent)
    val startX = extent.width
    plot.components.filter { a =>
      a.position == PlotComponent.Right
    }.reverse.foldLeft((startX, empty)) { case ((x, d), a) =>
      val rendered = a.render(subplot, pextent)
      val newX = x - rendered.extent.width
      (newX, Translate(rendered, x = newX, y = subplot.plotOffset.y + plot.plotOffset.y) behind d)
    }._2
  }

  private def overlayComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent
  ): Drawable = {
    // Overlays will be offset to the start of the plot area.
    val pextent = subplot.plotExtent(subplotExtent)
    plot.components.filter(_.position == PlotComponent.Overlay).map { a =>
      Translate(
        a.render(subplot, pextent),
        x = subplot.plotOffset.x,
        y = subplot.plotOffset.y
      )
    }.group
  }

  private def backgroundComponentRenderer[T](
    plot: Plot[FacetData],
    subplot: Plot[T],
    subplotExtent: Extent
  ): Drawable = {
    // The background will be offset to the start of the plot area.
    val pextent = subplot.plotExtent(subplotExtent)
    plot.components.filter(_.position == PlotComponent.Background).map { a =>
      Translate(
        a.render(subplot, pextent),
        x = subplot.plotOffset.x,
        y = subplot.plotOffset.y
      )
    }.group
  }

  private def facetedComponentRenderer(plot: Plot[FacetData], extent: Extent): (Drawable, Drawable) = {
    val plotExtent = plot.plotExtent(extent)
    val innerExtent = computeSubplotExtent(plot, plotExtent)
    val paddedPlots = updatePlotsForFacet(plot, innerExtent)

    // Each top component gets rendered for each column.
    val top = paddedPlots.head.zipWithIndex.map { case (subplot, i) =>
      val x = i * innerExtent.width
      Translate(topComponentRenderer(plot, subplot, innerExtent), x = x)
    }.group

    // Each bottom component gets rendered for each column.
    val bottom = paddedPlots.head.zipWithIndex.map { case (subplot, i) =>
      val x = i * innerExtent.width
      Translate(bottomComponentRenderer(plot, subplot, innerExtent, extent), x = x)
    }.group

    // Each left component gets rendered for each row.
    val left = paddedPlots.transpose.head.zipWithIndex.map { case (subplot, i) =>
      val y = i * innerExtent.height
      Translate(leftComponentRenderer(plot, subplot, innerExtent), y = y)
    }.group

    // Each right component gets rendered for each row.
    val right = paddedPlots.transpose.head.zipWithIndex.map { case (subplot, i) =>
      val y = i * innerExtent.height
      Translate(rightComponentRenderer(plot, subplot, innerExtent, extent), y = y)
    }.group

    // Overlays and backgrounds are plotted for each graph.
    val overlaysAndBackgrounds = paddedPlots.zipWithIndex.flatMap { case (row, yIndex) =>
      val y = yIndex * innerExtent.height
      row.zipWithIndex.map { case (subplot, xIndex) =>
        val x = xIndex * innerExtent.width
        val overlay = Translate(overlayComponentRenderer(plot, subplot, innerExtent), x = x, y = y)
        val background = Translate(backgroundComponentRenderer(plot, subplot, innerExtent), x = x, y = y)
        (overlay, background)
      }
    }
    val overlay = overlaysAndBackgrounds.map(_._1).group
    val background = overlaysAndBackgrounds.map(_._2).group

    (Group(Seq(top, bottom, left, right, overlay)), background)
  }

  def apply(plots: Seq[Seq[Plot[_]]]): Plot[FacetData] = {

    // X bounds for each column.
    val columnXBounds = plots.transpose.map(col => Plot.combineBounds(col.map(_.xbounds)))

    // Y bounds for each row.
    val rowYBounds = plots.map(row => Plot.combineBounds(row.map(_.ybounds)))

    // Update bounds on subplots.
    val updatedPlots = plots.zipWithIndex.map { case (row, y) =>
      row.zipWithIndex.map { case (subplot, x) =>
        subplot.xbounds(columnXBounds(x)).ybounds(rowYBounds(y))
      }
    }

    Plot[FacetData](
      data = updatedPlots,
      xbounds = Plot.combineBounds(columnXBounds),
      ybounds = Plot.combineBounds(rowYBounds),
      renderer = facetedPlotRenderer,
      componentRenderer = facetedComponentRenderer
    )
  }
}

