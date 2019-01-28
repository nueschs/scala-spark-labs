package com.scigility.lab.lab04

import com.scigility.lab.lab04.Lists._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class ListsSpec extends FunSuite with GeneratorDrivenPropertyChecks {

  implicit val peopleGen: Arbitrary[Person] = Arbitrary(Gen.resultOf(Person))

  val namesGen = Gen.listOfN(10, arbitrary[String])
  val agesGen = Gen.listOfN(10, arbitrary[Int])

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

  test("first names") {
    forAll { people: List[Person] =>
      firstNames(people) == people.map(_.firstName)
    }
  }

  test("old people") {
    forAll { (people: List[Person], minAge: Int) =>
      oldPeople(people, minAge).forall(_.age >= minAge)
    }
  }

  test("construct list of people") {
    forAll(namesGen, namesGen, agesGen) {(fns: List[String], lns: List[String], as: List[Int]) => {
      val expected = (fns zip lns zip as) map {
        case ((firstName, lastName), age) => Person(firstName, lastName, age)
      }

      constructPeopleList(fns, lns, as) == expected
    }}
  }
}
