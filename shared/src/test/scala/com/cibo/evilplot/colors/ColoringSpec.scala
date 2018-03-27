package com.cibo.evilplot.colors

import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultElements, DefaultFonts}
import com.cibo.evilplot.plot.aesthetics.{Colors, Elements, Fonts, Theme}
import org.scalatest.{FunSpec, Matchers}

class ColoringSpec extends FunSpec with Matchers {
  describe("multi color gradient construction") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should return a function when Colors has only one element") {
      val min: Double = 0
      val max: Double = 100
      val coloring = GradientUtils.multiGradient(Seq(HTMLNamedColors.blue), min, max)
      (min to max by 1.0).foreach(datum => coloring(datum) shouldBe HTMLNamedColors.blue)
    }

    it("should throw an exception when Colors is empty") {
      an[IllegalArgumentException] shouldBe thrownBy(GradientUtils.multiGradient(Seq(), 0, 100))
    }

    it("should return a function that works between min and max") {
      val data: Seq[Double] = Seq(0, 5, 20, 40, 70, 100)
      val gradient = ContinuousColoring.gradient(HTMLNamedColors.red, HTMLNamedColors.blue)

      val coloring = gradient(data)
      data.foreach(d => noException shouldBe thrownBy(coloring(d)))
    }
  }
  describe("coloring from the theme") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultColors => AesColors}
    implicit val overriddenTheme: Theme = new Theme {
      val fonts: Fonts = DefaultFonts()
      val elements: Elements = DefaultElements()
      val colors: Colors = AesColors().copy(stream = Seq(HTMLNamedColors.red))
    }
    it("should fail to color when the theme doesn't have enough colors") {
      val data = 0 to 5
      an[IllegalArgumentException] shouldBe thrownBy(CategoricalColoring.themed[Int].apply(data))
    }
  }
  describe("making a coloring out of a custom mapping") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should actually use the mapping") {
      val f = (s: String) => if (s == "hello") HTMLNamedColors.blue else HTMLNamedColors.red
      val coloring = CategoricalColoring.fromFunction(Seq("hello", "world"), f)
      val extractedFunc = coloring(Seq("hello", "world"))
      extractedFunc("hello") shouldBe HTMLNamedColors.blue
      extractedFunc("world") shouldBe HTMLNamedColors.red
    }
  }
}
