package com.scigility.lab.lab05

import com.scigility.lab.lab05.Lists._
import org.scalacheck._
import org.scalacheck.ScalacheckShapeless._

object ListsTest extends Properties("Tests for the Lab 05 (Lists)") {
  import Prop.forAll

  val peopleGen = Arbitrary(Gen.resultOf(Person))

//  property("lowercase first names") =

  property("old people") = forAll { (people: List[Person], minAge: Int) =>
    oldPeople(people, minAge).forall(_.age >= minAge)
  }

  property("first names") = forAll { people: List[Person] =>
    firstNames(people) ==  people.map(_.firstName)
  }


}
