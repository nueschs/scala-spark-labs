package com.scigility.lab.lab06.solution

import com.scigility.lab.lab06.solution.MyEither._
import org.scalacheck.Gen
import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks


class MyEitherTest extends FunSuite with GeneratorDrivenPropertyChecks {

  val eitherGen: Gen[Either[String, Int]] = Gen.chooseNum(-10, 10).flatMap{ i =>
    if(i % 2 == 0) Gen.chooseNum(-10, 10).map(Right[String, Int])
    else Gen.alphaLowerStr.map(Left[String, Int])
  }

  test("test Map") {
    forAll(eitherGen) { e: Either[String, Int] =>
      map(eitherToMyEither(e))(_ * 0.1) == eitherToMyEither(eitherMap(e)(_ * 0.1))
    }
  }

  test("test flatMap"){
    forAll(eitherGen) { e : Either[String, Int] =>
      flatMap[String, Int, Double](eitherToMyEither(e))(i => if (i < 0) MyLeft("Below Zero") else MyRight(i*0.1)) ==
        eitherToMyEither(eitherFlatMap[String, Int, Double](e)(i => if (i < 0) Left("Below Zero") else Right(i*0.1)))
    }
  }

  def myEitherToEither[A, B](e: MyEither[A, B]): Either[A, B] = e match {
    case MyLeft(l) => Left(l)
    case MyRight(r) => Right(r)
  }

  def eitherToMyEither[A, B](e: Either[A, B]): MyEither[A, B] = e match {
    case Left(a) => MyLeft[A,B](a)
    case Right(b) => MyRight[A, B](b)
  }

  /**
    * From Scala version  2.12
    */
  def eitherMap[A, B1, B2](e: Either[A, B1])(f: B1 => B2): Either[A, B2] = e match {
    case Right(b) => Right(f(b))
    case _        => e.asInstanceOf[Either[A, B2]]
  }

  /**
    * From Scala version  2.12
    */
  def eitherFlatMap[A, B1, B2](e: Either[A, B1])(f: B1 => Either[A, B2]): Either[A, B2] = e match {
    case Right(b) => f(b)
    case _ => e.asInstanceOf[Either[A, B2]]
  }
}
