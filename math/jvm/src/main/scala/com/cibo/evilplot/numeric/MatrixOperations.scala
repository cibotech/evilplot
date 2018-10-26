package com.cibo.evilplot.numeric
import breeze.linalg.DenseMatrix
import breeze.util.JavaArrayOps

object MatrixOperations extends MatrixOperationsI {
  def matrixMatrixTransposeMult(a: Array[Array[Double]], b: Array[Array[Double]]): Array[Array[Double]] = {

    val aMat = JavaArrayOps.array2DToDm(a)
    val bMat = JavaArrayOps.array2DToDm(b)

    JavaArrayOps.dmDToArray2(aMat * bMat.t)
  }
}
