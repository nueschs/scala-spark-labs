package com
package scigility
package day3

import cats._
import scalikejdbc._
import Program._

abstract class HistoryStore {

  def getHistory(k:String):List[State]
  def getMostRecent(k:String):Option[State] = getHistory(k).sorted(Order[State].toOrdering).headOption
  def appendState(k:String, v:State):Unit
  def withCurrentStateDo[X](k:String)(ifEmpty: => X)(vf: State => X):X = getMostRecent(k).fold(ifEmpty)(vf)
  def update(k:String)(ifEmpty: => State)(vf: State => State):Unit = appendState(k,withCurrentStateDo(k)(ifEmpty)(vf))
  def close:Unit
}

object HistoryStore {
  def jdbcHistStore(className:String, url:String, user:String, password:String):HistoryStore = new HistoryStore {
    Class.forName(className)
    ConnectionPool.singleton(url, user, password)

    val s = DBState.syntax("s")

    def getHistory(k:String):List[State] = {
      sql"select ${s.result.*} from ${DBState.as(s)} where ${s.parcelId} = ${k}".map(DBState(s.resultName)).list.apply().map(DBState.dec)
    }


    def appendState(k:String, v:State):Unit = {
      val dbs = DBState.enc(v)
      sql"insert into ${DBState.table} (${s.demarcator}, ${s.parcelId}, ${s.ts}, ${s.a1FN}, ${s.a1LN}, ${s.a1S}, ${s.a1ZIP}) values (${dbs.demarcator}, ${dbs.parcelId}, ${dbs.ts}, ${dbs.a1FN}, ${dbs.a1LN}, ${dbs.a1S}, ${dbs.a1ZIP})"
        .update.apply()
    }


    
  }
}
