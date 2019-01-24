package com.scigility.lab.lab05

object Lists {

  // example lists
  val firstNames = List("Cersei", "Ford", "Ellen", "Darth", "Arthur", "John")
  val lastNames = List("Lannister", "Prefect", "Ripley", "Vader", "Dent", "Snow")
  val ages = List(39, 42, 30, 45, 30, 22)

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

  /*
   * From lists of first names, last names and ages, define a function which constructs a list of persons
   *
   * s.t. constructPeopleList(firstNames, lastNames, ages) ==
   *   List(
	 *     Person("Cersei", "Lannister", 39),
	 *     Person("Ford", "Prefect", 42),
	 *     Person("Ellen", "Ripley", 30),
	 *     Person("Darth", "Vader", 45),
	 *     Person("Arthur", "Dent", 30),
	 *     Person("John", "Snow", 22)
   *   )
   */
  def constructPeopleList(firstNames: List[String], lastNames: List[String], ages: List[Int]): List[Person] = ???
}


