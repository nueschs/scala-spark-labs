package com.scigility.lab.lab06.solution

sealed trait MyOption[A]
final case class MySome[A](x: A) extends MyOption[A]
final case class MyNone[A]() extends MyOption[A]

object MyOption{
  def map[A, B](a: MyOption[A])(f: A => B): MyOption[B] = a match {
    case MyNone() => MyNone[B]
    case MySome(a) => MySome(f(a))
  }

  def flatMap[A, B](a: MyOption[A])(f: A => MyOption[B]): MyOption[B] = a match {
    case MyNone() => MyNone[B]
    case MySome(a) => f(a)
  }

  def filter[A](a: MyOption[A])(f: A => Boolean): MyOption[A] = a match {
    case MyNone() => MyNone[A]
    case MySome(a) => if (f(a)) MySome(a) else MyNone[A]
  }

  def isEmpty[A](a: MyOption[A]): Boolean = a match {
    case MyNone() => true
    case MySome(_) => false
  }

  def get[A](a: MyOption[A]): A = a match {
    case MyNone() => throw new NoSuchElementException
    case MySome(x) => x
  }
}