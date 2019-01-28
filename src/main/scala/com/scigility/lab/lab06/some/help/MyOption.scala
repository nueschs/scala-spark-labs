package com.scigility.lab.lab06.some.help

sealed trait MyOption[A]
final case class MySome[A](x: A) extends MyOption[A]
final case class MyNone[A]() extends MyOption[A]

/*
 * The Option is provided as an ADT, along with the function signatures. What you still need to do is think about what
 * cases can occur an how to handle them for each of the higher order functions below.
 *
 * If you have trouble coming up with these distinctions, look at package silver.plate.

 * When you are done, you can test your solution by copying MyOptionTest into package
 * com.scigility.lab.lab06.some.help under src/test/scala, adapting the import accordingly,
 * and finally running the test class
 */
object MyOption{
  def map[A, B](a: MyOption[A])(f: A => B): MyOption[B] = ???

  def flatMap[A, B](a: MyOption[A])(f: A => MyOption[B]): MyOption[B] = ???

  def filter[A](a: MyOption[A])(f: A => Boolean): MyOption[A] = ???

  def isEmpty[A](a: MyOption[A]): Boolean = ???

  def get[A](a: MyOption[A]): A = ???
}