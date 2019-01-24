package com.scigility.lab.lab05

import com.scigility.lab.lab05.Lists._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FunSuite, Matchers}

object ListsSpec extends FunSuite with GeneratorDrivenPropertyChecks with Matchers {

  implicit val peopleGen: Arbitrary[Person] = Arbitrary(Gen.resultOf(Person))

  test("lower") {
    forAll { l: List[String] =>
      lower(l) == l.map(_.toLowerCase)
    }
  }

  test("length") {
    forAll { l: List[String] =>
      lengths(l) == l.map(_.length)
    }
  }

  test("characters") {
    forAll { l : List[String] =>
      characters(l) == l.flatMap(_.toList)
    }
  }

  test("unique characters"){
    forAll { l: List[String] =>
      uniqueCharacters(l) == characters(l).distinct
    }
  }

  test("old people") {
    forAll { (people: List[Person], minAge: Int) =>
      oldPeople(people, minAge).forall(_.age >= minAge)
    }
  }

  test("first names") {
    forAll { people: List[Person] =>
      firstNames(people) == people.map(_.firstName)
    }
  }

}
