package com.cibo.evilplot.interaction

import com.cibo.evilplot.geometry.{InteractionEvent, OnClick}
import org.scalatest.{FunSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.Try

class InteractionMaskSpec extends FunSpec with Matchers {
  
  describe("The Interaction Mask"){

    it("Doesn't collide at <= 10,000"){
      import scala.concurrent.ExecutionContext.Implicits.global

      val failIfMoreThanAminute = Future {
        val testInteractionMask = new InteractionMask {
          override protected def getImageData(x: Double, y: Double): Array[Int] = Array(0, 0, 0, 0)

          def add(i: InteractionEvent): Unit = addEvents(Seq(i))
        }
        testInteractionMask.clearEventListeners()

        (0 until 10000).foreach( _ => testInteractionMask.add(OnClick(() => ())))
      }

      Await.result(failIfMoreThanAminute, 3.minutes)// probably should use ScalaFutures mixin

    }

  }

}
