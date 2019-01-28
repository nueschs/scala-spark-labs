package com.scigility.lab.lab03

import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.scigility.lab.lab03.Color._
import org.scalacheck.Gen

class ColorTest extends FunSuite with GeneratorDrivenPropertyChecks {

  val nonColorString: Gen[String] = Gen.alphaStr.suchThat(s => !List("red", "green", "blue").contains(s))

  test("test colors") {
    forAll(nonColorString, nonColorString) { (nonColor1: String, nonColor2: String) =>
      decodeColor(nonColor1) == decodeColor(nonColor2) &&
      decodeColor(nonColor1) != decodeColor("red") &&
      decodeColor(nonColor1) != decodeColor("green") &&
      decodeColor(nonColor1) != decodeColor("blue") &&
      decodeColor("red") != decodeColor("green") &&
      decodeColor("red") != decodeColor("blue") &&
      decodeColor("green") != decodeColor("blue") &&
      decodeColor("GreEn") == decodeColor("green")
    }
  }

}
