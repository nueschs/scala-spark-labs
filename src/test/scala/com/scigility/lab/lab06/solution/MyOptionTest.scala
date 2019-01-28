package com.scigility.lab.lab06.solution

import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import com.scigility.lab.lab06.solution.MyOption._

import scala.util.{Failure, Success, Try}

class MyOptionTest extends FunSuite with GeneratorDrivenPropertyChecks {

  val intOptionGen: Gen[Option[Int]] = Gen.chooseNum(-10, 10).map {
    case i if i % 2 == 0 => Some(i)
    case _ => None
  }

  val stringOption: Gen[Option[String]] = Gen.alphaStr.map {
    case s if s.length > 7 => Some(s)
    case _ => None
  }

  test("test mapping int options") {
    forAll(intOptionGen){o: Option[Int] =>
      map(optionToMyOption(o))(i => ";"+i) == optionToMyOption(o.map(i => ";"+i))
    }
  }

  test("test mapping string options") {
    forAll(stringOption){o: Option[String] =>
      map(optionToMyOption(o))(_.reverse) == optionToMyOption(o.map(_.reverse))
    }
  }

  test("test mapping to different type") {
    forAll(stringOption){o: Option[String] =>
      map(optionToMyOption(o))(_.length) == optionToMyOption(o.map(_.length))
    }
  }

  test("test flatMapping int options") {
    forAll(intOptionGen){o: Option[Int] =>
      flatMap[Int, Int](optionToMyOption(o))(i => if (i % 3 == 0) MySome(i) else MyNone()) ==
        optionToMyOption(o.flatMap(i => if (i % 3 == 0) Some(i) else None))
    }
  }

  test("test flatMapping string options") {
    forAll(stringOption){o: Option[String] =>
      flatMap[String, String](optionToMyOption(o))(s =>
        if (s.exists(('a' to 'n').contains)) MySome(s) else MyNone()) ==
      optionToMyOption(o.flatMap(s =>
        if (s.exists(('a' to 'n').contains)) Some(s) else None))
    }
  }

  test("test flatMapping to different type") {
    forAll(stringOption){o: Option[String] =>
      flatMap[String, Int](optionToMyOption(o))(s =>
        if (s.length > 12) MySome(s.length) else MyNone()) ==
      optionToMyOption(o.flatMap(s =>
        if (s.length > 12) Some(s.length) else None))
    }
  }

  test("filtering int options") {
    forAll(intOptionGen) { o: Option[Int] =>
      filter(optionToMyOption(o))(_ % 3 == 0) == optionToMyOption(o.filter(_ % 3 == 0))
    }
  }

  test("filtering String options") {
    forAll(stringOption){o: Option[String] =>
      filter(optionToMyOption(o))(_.length > 10) == optionToMyOption(o.filter(_.length > 10))
    }
  }

  test("test is empty"){
    forAll(intOptionGen, stringOption) { (oi: Option[Int], os: Option[String]) =>
      isEmpty(optionToMyOption(oi)) == oi.isEmpty &&
      isEmpty(optionToMyOption(os)) == os.isEmpty
    }
  }

  test("get works similar"){
    forAll(intOptionGen){ oi: Option[Int] =>
      (Try(get(optionToMyOption(oi))), Try(oi.get)) match {
        case (Failure(_), Failure(_)) => true
        case (Success(_), Success(_)) => true
        case _ => false
      }
    }
  }

  def optionToMyOption[A](o: Option[A]): MyOption[A] = o match {
    case Some(x) => MySome(x)
    case None => MyNone()
  }

}
