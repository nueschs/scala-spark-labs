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

  // hint: look at the recursion used in myMap above.
  def filter[A](lst: MyList[A])(predicate: A => Boolean): MyList[A] = lst match {
    case MyNil() => ???
    case MyCons(head, tail) => if (predicate(head)) ??? else ???
  }

  def zip[A, B](lstA: MyList[A], lstB: MyList[B]): MyList[(A, B)] = (lstA, lstB) match {
    case (MyNil(), MyNil()) => ???

    // The next two cases handle lists of different lengths. Think about how to handle those
    case (MyNil(), MyCons(headB, tailB)) => ???
    case (MyCons(headA, tailA), MyNil()) => ???

    case (MyCons(headA, tailA), MyCons(headB, tailB)) => ???
  }


  def foldLeft[A, B](lst: MyList[A], b: B)(f: (B, A) => B): B = lst match {
    case MyNil() => ???
    case MyCons(head, tail) => ??? // foldLeft(???)

  }

}