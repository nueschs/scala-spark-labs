package com.scigility.lab.lab10.pageview

case class PageView(url: String, status: Int, zipCode: Int, userID: Int)
  extends Serializable {
  override def toString: String = {
    "%s\t%s\t%s\t%s".format(url, status, zipCode, userID)
  }
}

object PageView extends Serializable {
  def fromString(in: String): PageView = {
    val parts = in.split("\t")
    new PageView(parts(0), parts(1).toInt, parts(2).toInt, parts(3).toInt)
  }
}
