/*
 * Copyright (c) 2018, CiBO Technologies, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.cibo.evilplot.plot

import org.scalatest.{FunSpec, Matchers}

class ExplcitImplicitsSpec extends FunSpec with Matchers {
  describe("ExplicitImplicit") {
    it("allows default implicit themes"){
       val hist = Histogram(Seq(1, 2, 2)) 
       hist.render().extent shouldBe Plot.defaultExtent
    }

    it("Provides a mechansim to *require* explicit themes at compile time"){
      import com.cibo.evilplot.plot.aesthetics.Theme
      object CustomHistogram extends ExplicitImplicits{
        def hist(xs:Seq[Double])(implicit theme:Theme) = Histogram(xs)(theme).render()(theme)
      }
      CustomHistogram.hist( Seq(1,2,2) ).extent shouldBe Plot.defaultExtent
    }

    it("still provides a mechansim to catch accidental default themes at compile time with a type Error"){

      assertTypeError("""
        import com.cibo.evilplot.plot.aesthetics.Theme
        object CustomHistogram extends ExplicitImplicits{
          def hist(xs:Seq[Double])(implicit theme:Theme) = Histogram(xs).render()    // both implicit
        }
      """)
      assertTypeError("""
        import com.cibo.evilplot.plot.aesthetics.Theme
        object CustomHistogram extends ExplicitImplicits{
          def hist(xs:Seq[Double])(implicit theme:Theme) = Histogram(xs)(theme).render()  // first explicit
        }
      """)
      assertTypeError("""
        import com.cibo.evilplot.plot.aesthetics.Theme
        object CustomHistogram extends ExplicitImplicits{
          def hist(xs:Seq[Double])(implicit theme:Theme) = Histogram(xs)().render()(theme)  //second explicit
        }
      """)
      assertCompiles("""
        import com.cibo.evilplot.plot.aesthetics.Theme
        object CustomHistogram extends ExplicitImplicits{
          def hist(xs:Seq[Double])(implicit theme:Theme) = Histogram(xs)(theme).render()(theme)  //both explicit
        }
      """)
    }
  }
}
