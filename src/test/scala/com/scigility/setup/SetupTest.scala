package com.scigility.setup

import org.scalacheck._

object SetupTest extends Properties("Simple Setup Test") {
  import Prop.forAll

  property("setupTest") = forAll { (l1: List[Int], l2: List[Int]) =>  
    l1.size + l2.size == (l1 ::: l2).size 
  }
  
}
