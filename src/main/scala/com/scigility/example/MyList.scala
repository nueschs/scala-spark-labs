package com.scigility.example

sealed trait MyList[A]
final case class MyCons[A](head: A, tail: MyList[A]) extends MyList[A]
final case class MyNil[A]() extends MyList[A]
