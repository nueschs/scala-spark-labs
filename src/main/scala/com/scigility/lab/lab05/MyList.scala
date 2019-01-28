package com.scigility.lab.lab05

sealed trait MyList[A]
final case class MyCons[A](head: A, tail: MyList[A]) extends MyList[A]
final case class MyNil[A]() extends MyList[A]


object MyList {
  def myMap[A, B](lst: MyList[A])(f: A => B): MyList[B] = lst match {
    case MyNil() => MyNil[B]
    case MyCons(head, tail) => MyCons(f(head), myMap(tail)(f))
  }

  /**
    * Filters a list based on a predicate
    *
    * @return the list of all elements in lst which satisfy the predicate
    */
  def filter[A](lst: MyList[A])(predicate: A => Boolean): MyList[A] = ???

  /**
    * Simultaneously traverses lstA and lstB zipping the lists
    *
    * @return a list of pairs (A, B) of the elements at the same index in each list
    */
  def zip[A, B](lstA: MyList[A], lstB: MyList[B]): MyList[(A, B)] = ???

  /**
    * Traverses a list from left to right, accumulating the result of f and starting with "zero" value b
    */
  def foldLeft[A, B](lst: MyList[A], b: B)(f: (B, A) => B): B = ???
}