package com.scigility.lab.lab06.silver.plate

sealed trait MyOption[A]
final case class MySome[A](x: A) extends MyOption[A]
final case class MyNone[A]() extends MyOption[A]

/*
 * Try to implement the handling of each of the different cases.
 *
 * When you are done, you can test your solution by copying MyOptionTest into package
 * com.scigility.lab.lab06.silver.plate under src/test/scala, adapting the import accordingly,
 * and finally running the test class
 */
object MyOption{
  def map[A, B](a: MyOption[A])(f: A => B): MyOption[B] = a match {
    case MyNone() => ???
    case MySome(a) => ???
  }

  def flatMap[A, B](a: MyOption[A])(f: A => MyOption[B]): MyOption[B] = a match {
    case MyNone() => ???
    case MySome(a) => ???
  }

  def filter[A](a: MyOption[A])(f: A => Boolean): MyOption[A] = a match {
    case MyNone() => ???
    case MySome(a) => ???
  }

  def isEmpty[A](a: MyOption[A]): Boolean = a match {
    case MyNone() => ???
    case MySome(_) => ???
  }

  def get[A](a: MyOption[A]): A = a match {
    case MyNone() => ???
    case MySome(x) => ???
  }
}