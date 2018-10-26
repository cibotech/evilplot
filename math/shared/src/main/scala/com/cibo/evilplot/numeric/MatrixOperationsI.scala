package com.cibo.evilplot.numeric

trait MatrixOperationsI {
  def matrixMatrixTransposeMult(
    a: Array[Array[Double]],
    b: Array[Array[Double]]): Array[Array[Double]]
}
