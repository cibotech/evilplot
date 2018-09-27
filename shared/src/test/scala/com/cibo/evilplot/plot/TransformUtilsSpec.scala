package com.cibo.evilplot.plot

import com.cibo.evilplot.geometry.Extent
import com.cibo.evilplot.numeric.{Bounds, Point}
import org.scalatest.{FunSpec, Matchers}

class TransformUtilsSpec extends FunSpec with Matchers {


  describe("PlotUtils"){

    it("computes the correct buffer"){
      val zeroTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, 10.0), 0.0)
      zeroTen shouldEqual Bounds(0.0, 10.0)

      val zeroTenTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, 10.0), 0.1)
      zeroTenTen shouldEqual Bounds(-1.0, 11.0)

      val negZeroTenTen = PlotUtils.boundsWithBuffer(xs = Seq(0.0, -10.0), buffer = 0.1)
      negZeroTenTen shouldEqual Bounds(-11.0, 1.0)
    }

    it("computes bounds"){
      val points = Seq(Point(0.0, 0.0), Point(10.0, 10.0))

      PlotUtils.bounds(points, 0.1) shouldEqual (Bounds(-1.0, 11.0), Bounds(-1.0, 11.0))
      PlotUtils.bounds(points, 0.0, xboundBuffer = Some(0.1)) shouldEqual (Bounds(-1.0, 11.0), Bounds(0, 10.0))
      PlotUtils.bounds(points, 0.0, yboundBuffer = Some(0.1))shouldEqual (Bounds(0, 10.0), Bounds(-1.0, 11.0))
    }

  }

  describe("TransformWorldToScreen"){

    val xTransformer = TransformWorldToScreen.xCartesianTransformer(Bounds(0, 100), extent = Extent(100, 100))
    val yTransformer = TransformWorldToScreen.yCartesianTransformer(Bounds(0, 100), extent = Extent(100, 100))

    it("default x transformer works properly"){

      xTransformer(-100) shouldEqual -100.0 +- 0.000001
      xTransformer(0) shouldEqual 0.0 +- 0.000001
      xTransformer(100) shouldEqual 100.0 +- 0.000001

    }

    it("default y transformer works properly"){

      yTransformer(-100) shouldEqual 200.0 +- 0.000001
      yTransformer(0) shouldEqual 100.0 +- 0.000001
      yTransformer(100) shouldEqual 0.0 +- 0.000001
    }

    it("Transforms to screen correctly"){
      import TransformWorldToScreen._
      val transformer = TransformWorldToScreen.yCartesianTransformer(Bounds(0, 10), extent = Extent(100, 100))

      transformDatumToWorld(Point(0.0, 0.0), xTransformer, yTransformer) shouldEqual Point(0.0, 100.0)
    }
  }
}
