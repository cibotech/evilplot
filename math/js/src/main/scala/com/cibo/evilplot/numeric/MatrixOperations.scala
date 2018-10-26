package com.cibo.evilplot.numeric

object MatrixOperations extends MatrixOperationsI {

  def matrixMatrixTransposeMult(
    a: Array[Array[Double]],
    b: Array[Array[Double]]): Array[Array[Double]] = {
    require(
      a.head.length == b.head.length,
      "matrix multiplication is not defined for matrices whose" +
        " inner dimensions are not equal")
    val innerIndices = a.head.indices
    Array.tabulate[Double](a.length, b.length) {
      case (i, j) =>
        innerIndices.foldLeft(0.0) { (total, k) =>
          total + a(i)(k) * b(j)(k)
        }
    }
  }
}
