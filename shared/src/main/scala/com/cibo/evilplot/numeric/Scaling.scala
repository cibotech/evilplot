package com.cibo.evilplot.numeric

case class LinearScaling(in: (Double, Double), out: (Double, Double)) {
  def scale(x: Double): Double = {
    val inDiff = in._2 - in._1
    val outDiff = out._2 - out._1
    ((x - in._1) / inDiff) * outDiff + out._1
  }
}

/*
private[plot] case class DefaultXTransformer() extends Transformer {
  def apply(plot: Plot, plotExtent: Extent): Double => Double = {
    val scale = plotExtent.width / plot.xbounds.range
    (x: Double) =>
      (x - plot.xbounds.min) * scale
  }
}
*/
