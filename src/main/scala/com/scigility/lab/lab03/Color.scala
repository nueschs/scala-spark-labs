package com.scigility.lab.lab03

sealed trait Color
case class Red() extends Color
case class Green() extends Color
case class Blue() extends Color
case object Unknown extends Color

object Color{
  def decodeColor(s: String): Color = s.toLowerCase match {
    case "red" => Red()
    case "green" => Green()
    case "blue" => Blue()
    case _ => Unknown
  }
}
