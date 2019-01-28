package com.scigility.lab.lab05

import com.scigility.lab.lab05.MyListScaffold._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class MyListScaffoldTest extends FunSuite with GeneratorDrivenPropertyChecks {

  val longList: Gen[List[String]] = Gen.listOfN(10, arbitrary[String])
  val shortList: Gen[List[Int]] = Gen.listOfN(5, arbitrary[Int])

  test("filter int list") {
    forAll { (lst: List[Int], i: Int) =>
      toList(filter(toMyList(lst))(_ > i)) == lst.filter(_ > i)
    }
  }

  test("filter string list") {
    forAll { (lst: List[String], l: Int) =>
      toList(filter(toMyList(lst))(_.length <= l)) == lst.filter(_.length <= l)
    }
  }

  test("zip two lists test"){
    forAll { (lst1: List[String], lst2: List[Int]) =>
      toList(zip(toMyList(lst1), toMyList(lst2))) == lst1.zip(lst2)
    }
  }

  // This could also be failing, depending on how the implementation treats 
  // lists of different sizes
  test("zip two lists of different sizes"){
    forAll(longList, shortList) { (lst1: List[String], lst2: List[Int]) =>
      toList(zip(toMyList(lst1), toMyList(lst2))) == lst1.zip(lst2)
    }
  }
  
  test("fold left test"){
    def op: (String, Int) => String = (result, i) => s"$result,$i"
    forAll { lst: List[Int] =>
      foldLeft(toMyList(lst), "")(op) == lst.foldLeft("")(op)
    }
  }
  
  

  def toList[A](myList: MyList[A]): List[A] = myList match {
    case MyNil() => Nil
    case MyCons(head, tail) => head :: toList(tail)
  }

  def toMyList[A](lst: List[A]): MyList[A] = lst match {
    case Nil => MyNil[A]
    case h :: t => MyCons(h, toMyList(t))
  }
}
