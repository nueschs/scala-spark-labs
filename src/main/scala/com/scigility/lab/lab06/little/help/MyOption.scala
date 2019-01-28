package com.scigility.lab.lab06.little.help

/*
 * You can start your implementation from the minimal structure given here
 * if you're stuck, you can get some help in package some.help
 *
 * Optional: Once your done, look at MyOptionTest and try adapt it so that it tests your solution
 * (i.e. change package and type names accordingly, and see if it runs)
 */
sealed trait MyOption[A]
// think about how to model the different forms an option come in

object MyOption{
  /**
    * Transforms a MyOption into another MyOption of a different type
    */
  //def map

  /**
    * Similar to map, but here, the content of a MyOption is turned into a MyOption of a different type
    */
  //def flatMap

  /**
    * Looking at an MyOption, filter returns "something" if a predicate holds its content
    */
  //def filter

  /**
    * Returns true if the MyOption contains something, or false otherwise
    */
  //def isEmpty

  /**
    * Get the content of a MyOption
    */
  //def get
}