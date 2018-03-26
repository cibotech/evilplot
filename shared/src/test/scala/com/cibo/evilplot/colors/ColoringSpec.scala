package com.cibo.evilplot.colors

import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultColors, DefaultElements, DefaultFonts}
import com.cibo.evilplot.plot.aesthetics.Theme
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
      val gradient = ContinuousGradient.ofTwo(HTMLNamedColors.red, HTMLNamedColors.blue)

      val coloring = gradient(data)
      data.foreach(d => noException shouldBe thrownBy(coloring(d)))
    }
  }
  describe("nonsense for getting default coloring") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should not come to a screeching halt when the ATTR type is double") {
      val someData = Seq(1.2, 3.4, 5.6)
      noException shouldBe thrownBy(Coloring.default(someData))
    }
    it("should use the theme in all other cases") {
      val strings = Seq("!", "hello", "world")
      val coloring = Coloring.default(strings)
      val f = coloring(strings)
      f("!") shouldBe defaultTheme.colors.stream.head
      f("hello") shouldBe defaultTheme.colors.stream(1)
      f("world") shouldBe defaultTheme.colors.stream(2)
    }
  }
  describe("coloring from the theme") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme.{DefaultColors => AesColors}
    implicit val overriddenTheme: Theme = new Theme {
      val fonts: DefaultFonts = DefaultFonts()
      val elements: DefaultElements = DefaultElements()
      val colors: DefaultColors = AesColors().copy(stream = Seq(HTMLNamedColors.red))
    }
    it("should fail to color when the theme doesn't have enough colors") {
      val data = 0 to 5
      an[IllegalArgumentException] shouldBe thrownBy(ThemedCategorical[Int]().apply(data))
    }
  }
  describe("making a coloring out of a custom mapping") {
    import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
    it("should actually use the mapping") {
      val f = (s: String) => if (s == "hello") HTMLNamedColors.blue else HTMLNamedColors.red
      val coloring = CategoricalFromFunction(Seq("hello", "world"), f)
      val extractedFunc = coloring(Seq("hello", "world"))
      extractedFunc("hello") shouldBe HTMLNamedColors.blue
      extractedFunc("world") shouldBe HTMLNamedColors.red
    }
  }
}
