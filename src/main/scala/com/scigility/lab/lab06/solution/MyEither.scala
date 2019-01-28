package com.scigility.lab.lab06.solution

sealed trait MyEither[A, B]
final case class MyLeft[A, B](l: A) extends MyEither[A, B]
final case class MyRight[A, B](r: B) extends MyEither[A, B]

object MyEither {
  def map[A, B1, B2](either: MyEither[A, B1])(f: B1 => B2): MyEither[A, B2] = either match {
    case MyLeft(l) => MyLeft[A, B2](l)
    case MyRight(r) => MyRight[A, B2](f(r))
  }

  def flatMap[A, B1, B2](either: MyEither[A, B1])(f: B1 => MyEither[A, B2]) = either match {
    case MyLeft(l) => MyLeft[A, B2](l)
    case MyRight(r) => f(r)
  }
}


