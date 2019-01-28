package com.scigility.lab.lab06.little.help

/*
 * You can start your implementation from the minimal structure given here
 * if you're stuck, you can get some help in package some.help
 *
 * Optional: Once your done, look at MyEitherTest, copy it to package com.scigility.lab.lab06.little.help in
 * src/test/scala, and try adapt it so that it tests your solution (i.e. change package and type names accordingly,
 * and see if it runs)
 */
sealed trait MyEither[A, B]
// think about how to model the different forms Eithers could come in

object MyEither {
  /**
    * Transforms an Either into another type. Thinking back to the Refined Solution of handling errors shown before,
    * notice how the "success" case is on the right, and how only this case is considered in transformation
    */
  //def map


  /**
    * Similar to map (the same hints apply as well), however, flatMaps converts the content of an MyEither to another
    * MyEither
    */
  //def flatMap
}


