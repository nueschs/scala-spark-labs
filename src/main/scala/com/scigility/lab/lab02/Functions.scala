package com.scigility.lab.lab02

object Functions {
  def factorial(i:Int):Int = i match {
    case 0 => 1
    case k => k * factorial(k-1)
  }
}
