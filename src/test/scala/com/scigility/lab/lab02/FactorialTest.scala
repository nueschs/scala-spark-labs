package com.scigility.lab.lab02

import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.scigility.lab.lab02.Functions._
import org.scalacheck.Gen

class FactorialTest extends FunSuite with GeneratorDrivenPropertyChecks {

  test("factorial") {
    def fact(i: Int): Int = i match {
      case 0 => 1
      case k => k * fact(k-1)
    }

    forAll(Gen.choose(0, 10)) { i : Int =>
      factorial(i) == fact(i)
    }
  }
}
