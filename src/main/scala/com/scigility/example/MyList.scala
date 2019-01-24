package com.scigility.example

sealed trait MyList[A]
final case class MyCons[A](head: A, tail: MyList[A]) extends MyList[A]
final case class MyNil[A]() extends MyList[A]


object MyList {
  def myMap[A, B](lst: MyList[A])(f: A => B): MyList[B] = lst match {
    case MyNil() => MyNil[B]
    case MyCons(head, tail) => MyCons(f(head), myMap(tail)(f))
  }
}