package com.scigility.lab.lab05

/*
 * This object contains some hints on how to structure your solution of Lab 05,
 * and about which cases you have to think for each function
 */
object MyListScaffold {

  def myMap[A, B](lst: MyList[A])(f: A => B): MyList[B] = lst match {
    case MyNil() => MyNil[B]
    case MyCons(head, tail) => MyCons(f(head), myMap(tail)(f))
  }

  def filter[A](lst: MyList[A])(predicate: A => Boolean): MyList[A] = lst match {
    case MyNil() => MyNil[A]
    case MyCons(head, tail) => if (predicate(head)) MyCons(head, filter(tail)(predicate)) else filter(tail)(predicate)
  }

  def zip[A, B](lstA: MyList[A], lstB: MyList[B]): MyList[(A, B)] = (lstA, lstB) match {
    case (MyNil(), MyNil()) => MyNil[(A,B)]

    // We decided to handle lists of different lengths gracefully, i.e. zip all elements of the shorter list with
    // length n with the first n elements of the longer list, and discard the left overs (sam behaviour as in the
    // scala standard library)
    case (MyNil(), MyCons(headB, tailB)) => MyNil[(A, B)]
    case (MyCons(headA, tailA), MyNil()) => MyNil[(A, B)]

    case (MyCons(headA, tailA), MyCons(headB, tailB)) => MyCons((headA, headB), zip(tailA, tailB))
  }

  def foldLeft[A, B](lst: MyList[A], b: B)(f: (B, A) => B): B = lst match {
    case MyNil() => b
    case MyCons(head, tail) => foldLeft(tail, f(b, head))(f)
  }

}