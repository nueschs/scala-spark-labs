package com.scigility.lab.lab06.some.help

sealed trait MyEither[A, B]
final case class MyLeft[A, B](l: A) extends MyEither[A, B]
final case class MyRight[A, B](r: B) extends MyEither[A, B]

/*
 * The Either structure has been modelled as an ADT for you, and the function signatures are there. What you still
 * need to do is think about what cases can occur an how to handle them for each of the higher order functions below.
 *
 * If you have trouble coming up with these distinctions, look at package silver.plate.
 *
 * When you are done, you can test your solution by changing the imports in MyEitherTest from
 *    import com.scigility.lab.lab06.solution.MyEither._
 * to
 *    import com.scigility.lab.lab06.some.help.MyEither._
 *
 * and then running the test class
 */
object MyEither {
  def map[A, B1, B2](either: MyEither[A, B1])(f: B1 => B2): MyEither[A, B2] = ???

  def flatMap[A, B1, B2](either: MyEither[A, B1])(f: B1 => MyEither[A, B2]) = ???
}


