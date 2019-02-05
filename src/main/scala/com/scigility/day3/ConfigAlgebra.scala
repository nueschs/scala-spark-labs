package com
package scigility
package day3


trait ConfigAlgebra[F[_]] {

  def readConfigFromString[C](confString:String):F[C] = ???
}

object ConfigAlgebra {

  def apply[F[_]](implicit instance:ConfigAlgebra[F]):ConfigAlgebra[F] = instance
}
