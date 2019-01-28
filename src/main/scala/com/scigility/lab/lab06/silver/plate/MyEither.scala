package com.scigility.lab.lab06.silver.plate

sealed trait MyEither[A, B]
final case class MyLeft[A, B](l: A) extends MyEither[A, B]
final case class MyRight[A, B](r: B) extends MyEither[A, B]

/*
 * Try to implement the handling of each of the different cases. Remember, the transformations below only treat
 * the "success" side of things (i.e. the right side)
 *
 * When you are done, you can test your solution by copying MyEitherTest into package
 * com.scigility.lab.lab06.silver.plate under src/test/scala, adapting the import accordingly,
 * and finally running the test class
 */
object MyEither {
  def map[A, B1, B2](either: MyEither[A, B1])(f: B1 => B2): MyEither[A, B2] = either match {
    case MyLeft(l) => ???
    case MyRight(r) => ???
  }

  def flatMap[A, B1, B2](either: MyEither[A, B1])(f: B1 => MyEither[A, B2]) = either match {
    case MyLeft(l) => ???
    case MyRight(r) => ???
  }
}


