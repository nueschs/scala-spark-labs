package com.scigility.lab.lab05

object Lists {

  val firstNames = List("Cersei", "Ford", "Ellen", "Darth", "Arthur", "John")

  case class Person(firstName: String, name: String, age: Int)

  // transform all first names in the list above to lower case
  // s.t. lower(firstNames) == List("cersei", "ford", "ellen", "darth", "arthur", "john")
  def lower(names: List[String]): List[String] = ???

  // create a list which contains the length of each of the names in firstNames in the correct order
  // s.t. lengths(firstNames) == List(6, 4, 5, 5, 6, 4)
  def lengths(names: List[String]): List[Int] = ???

  // make a list of all characters in order of their appearance in firstNames
  // s.t. characters(firstNames) == List('C','e','r','s','e','i','F',...)
  def characters(names: List[String]): List[Char] = ???

  // remove duplicates from the above list of characters
  // there is a specific function for this available on list, but try to build it using foldLeft or something else first
  def uniqueCharacters(names: List[String]): List[Char] = ???

  // write a function which converts a list of persons to a list of their first names
  def firstNames(persons: List[Person]): List[String] = ???

  // write a function which takes a list of persons and returns the list of persons in that list which are older than
  // the minimal age
  def oldPeople(persons: List[Person], minAge: Int): List[Person] = ???

}
