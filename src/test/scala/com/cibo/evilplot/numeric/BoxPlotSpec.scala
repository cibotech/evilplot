package com.cibo.evilplot.numeric

import org.scalatest.{FunSpec, Matchers}

import scala.util.Random

class BoxPlotSpec extends FunSpec with Matchers {
  val tol = 1e-8
  val data = List(-2541.335733882479, 1577.0315624249806, -808.0673232141799, 680.9128930911302,
    -2445.2589645401004, -7.260674159999326, -1762.1261882364997, -776.52236318016, -3198.781083548529,
    517.4382306836906, -1982.1566564704299, -1700.7419477605)
  describe("BoxPlot") {
    it("should correctly calculate quartiles using linear interpolation between values") {
      val boxPlot = new BoxPlot(data)
      // NumPy on this list: [ np.percentile(data, x) for x in xrange(25, 100, 25) ] ==
      val (first, second, third) = (-2097.9322334878475, -1254.4046354873399, 123.91405205092315)
      // low tolerance because above data is only to hundredths place
      boxPlot.lowerQuantile shouldEqual first +- tol
      boxPlot.middleQuantile shouldEqual second +- tol
      boxPlot.upperQuantile shouldEqual third +- tol
    }

    it("should give the maximum when asked for the 1.0 quantile") {
      val boxPlot = new BoxPlot(data, quantiles = (0.0, 0.5, 1.0))
      boxPlot.upperQuantile shouldEqual data.max +- tol
    }

    it("0.5 quantile w linear interpolation should give the same answer as median (even number of elements in list)") {
      val medianData: Seq[Double] = Seq.fill(50)(Random.nextDouble())
      val sorted = medianData.sorted
      val median = (sorted(24) + sorted(25))/ 2.0
      val boxPlot = new BoxPlot(medianData)
      boxPlot.middleQuantile shouldEqual median +- tol
    }

    it("0.5 quantile w linear interpolation should give the same answer as median (odd number of elements in list)") {
      val medianData: Seq[Double] = Seq.fill(49)(Random.nextDouble())
      val sorted = medianData.sorted
      val median = sorted(24)
      val boxPlot = new BoxPlot(medianData)
      boxPlot.middleQuantile shouldEqual median +- tol
    }

    it("correctly classifies as outliers elements outside lowerQ - 1.5*IQR < x < upperQ + 1.5*IQR") {
      val temperatureData = Seq(94.371, 94.304, 94.216, 94.130, 94.050, 93.961, 93.840, 93.666, 93.430, 93.141,
      92.824, 92.515, 92.249, 92.048, 91.920, 91.853, 91.824, 91.810, 91.788, 91.747,
      91.685, 91.612, 91.547, 91.511, 91.520, 91.585, 91.710, 91.015, 91.898, 92.146, 92.451,
      92.800, 93.178, 93.573, 93.972, 94.360, 94.717, 95.010, 95.211, 95.295, 95.261,
      95.127, 94.932, 94.729, 94.565, 94.465, 94.429, 94.440, 94.478, 94.538, 94.632,
      94.775, 94.973, 95.202, 95.416, 95.561, 95.592, 95.490, 95.263, 94.945, 94.590,
      94.258, 94.003, 93.866, 93.868, 94.015, 94.296, 94.677, 95.107, 95.520, 95.853,
      96.058, 96.119, 96.053, 98.032, 95.906, 95.741, 95.616, 95.566, 95.591, 95.668, 95.756,
      95.817, 95.824, 95.759, 95.623, 95.432, 95.214, 95.002, 94.819, 94.675, 94.573,
      94.514, 94.507, 94.562, 94.682, 94.858, 95.067, 95.278, 95.463, 95.598, 95.664)
      val outliers = Seq(91.015, 98.032)
      val boxPlot = new BoxPlot(temperatureData)
      boxPlot.outliers.length shouldEqual outliers.length
      (boxPlot.outliers zip outliers).foreach { case (computed, actual) => computed shouldEqual actual +- tol }
    }
  }
}
