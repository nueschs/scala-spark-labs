package com
package scigility
package day3

import cats._
import scalikejdbc._
import Program._

//this is our abstraction to write to the state store. Normally this would have type parameters all over the place to allow for reuse
//however I opted to make it a bit more friendly to put in the specific types
//note that there's only 2 primitive methods and all others are expressed in terms of those. This is a very common pattern in scala.
//However it has a downside that e.g. getMostRecent could be deferred entirely to the database and be done more efficient
//in such a case you'd chose to overwrite this as well
trait HistoryStore {

  def getHistory(k:String):List[State]
  def getMostRecent(k:String):Option[State] = getHistory(k).sorted(Order[State].toOrdering).headOption
  def appendState(k:String, v:State):Unit
  def withCurrentStateDo[X](k:String)(ifEmpty: => X)(vf: State => X):X = getMostRecent(k).fold(ifEmpty)(vf)
  def update(k:String)(ifEmpty: => State)(vf: State => State):Unit = appendState(k,withCurrentStateDo(k)(ifEmpty)(vf))
  def close():Unit
}

object HistoryStore {
  //this is the actual implementation. This is really just given by what  SQL Library you chose
  def jdbcHistStore(className:String, url:String, user:String, password:String):HistoryStore = new HistoryStore {
    Class.forName(className)
    ConnectionPool.singleton(url, user, password)
    private  val connection = ConnectionPool.borrow()
    private val db: DB = DB(connection)
    val s = DBState.syntax("s")

    def getHistory(k:String):List[State] = db.localTx { implicit session =>
      sql"select ${s.result.*} from ${DBState.as(s)} where ${s.parcelId} = ${k}".map(DBState(s.resultName)).list.apply().map(DBState.dec _)
    }


    def appendState(k:String, v:State):Unit = {
      db.localTx { implicit session =>
        val dbs = DBState.enc(v)
        sql"insert into ${DBState.table} (${s.demarcator}, ${s.parcelId}, ${s.ts}, ${s.aOne}, ${s.aTwo}) values (${dbs.demarcator}, ${dbs.parcelId}, ${dbs.ts}, ${s.aOne}, ${s.aTwo})"
        .update.apply()
      }
      ()
    }

    def close():Unit = {
      db.close()
      connection.close()
    }


    
  }
}
