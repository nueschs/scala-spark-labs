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
  def filter[A](lst: MyList[A])(predicate: A => Boolean): MyList[A] = lst match {
    case MyNil() => MyNil[A]
    case MyCons(head, tail) => if (predicate(head)) MyCons(head, filter(tail)(predicate)) else filter(tail)(predicate)
  }

  /**
    * Simultaneously traverses lstA and lstB zipping the lists
    *
    * @return a list of pairs (A, B) of the elements at the same index in each list
    */
  def zip[A, B](lstA: MyList[A], lstB: MyList[B]): MyList[(A, B)] = (lstA, lstB) match {
    case (MyNil(), MyNil()) => MyNil[(A,B)]

    // We decided to handle lists of different lengths gracefully, i.e. zip all elements of the shorter list with
    // length n with the first n elements of the longer list, and discard the left overs (sam behaviour as in the
    // scala standard library)
    case (MyNil(), MyCons(headB, tailB)) => MyNil[(A, B)]
    case (MyCons(headA, tailA), MyNil()) => MyNil[(A, B)]

    case (MyCons(headA, tailA), MyCons(headB, tailB)) => MyCons((headA, headB), zip(tailA, tailB))
  }

  /**
    * Traverses a list from left to right, accumulating the result of f and starting with "zero" value b
    */
  def foldLeft[A, B](lst: MyList[A], b: B)(f: (B, A) => B): B = lst match {
    case MyNil() => b
    case MyCons(head, tail) => foldLeft(tail, f(b, head))(f)
  }
}