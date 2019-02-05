package com
package scigility
package day3

import io.circe._, io.circe.generic.semiauto._
import cats._
import scalikejdbc._

object Program {

  final case class Address(firstName:String, lastName:String, street:String, zipCode:Int)

  sealed trait Message{
    def msgKey:String
    def parcelId:String
    def ts:Long
  }
  final case class NewParcel(msgKey:String, parcelId:String, ts:Long, sender:Address, recipient:Address) extends Message
  final case class DistributionHop(msgKey:String, parcelId:String, ts:Long, distributionCenter:Address) extends Message
  final case class Delivery(msgKey:String, parcelId:String, ts:Long, accepted:Boolean) extends Message
  final case class DestinationChange(msgKey:String, parcelId:String, ts:Long, newRecipient: Address) extends Message

  sealed trait Response
  final case class MessageFailure(msgKey:String, reason:String) extends Response
  final case class MessageSuccess(msgKey:String) extends Response
  

  sealed trait State{
    def ts:Long
  }
  final case class New(parcelId:String, ts:Long, sender:Address, recipient:Address) extends State
  final case class InDelivery(parcelId:String, ts:Long, distributionCenter:Address, intendedRecipientAddress:Address) extends State
  final case class Delivered(parcelId:String, ts:Long, recipient:Address) extends State

  implicit val enc:Encoder[Response] = deriveEncoder[Response]
  implicit val decA:Decoder[Address] = deriveDecoder[Address]
  implicit val dec:Decoder[Message] = deriveDecoder[Message]

  implicit val order:Order[State] = Order.from[State]((a,a1) => a.ts.compare(a1.ts))

  final case class DBState(
    demarcator:String,
    parcelId:String,
    ts:Long,
    a1FN:Option[String],
    a1LN:Option[String],
    a1S:Option[String],
    a1ZIP:Option[Int],
    a2FN:Option[String],
    a2LN:Option[String],
    a2S:Option[String],
    a2ZIP:Option[Int]
  )

  object DBState  extends SQLSyntaxSupport[DBState] {
    override val tableName = "states"


    def apply(g: ResultName[DBState])(rs: WrappedResultSet) =
      new DBState(
        rs.string(g.demarcator), 
        rs.string(g.parcelId),
        rs.long(g.ts),
        rs.stringOpt(g.a1FN),
        rs.stringOpt(g.a1LN),
        rs.stringOpt(g.a1S),
        rs.intOpt(g.a1ZIP),
        rs.stringOpt(g.a2FN),
        rs.stringOpt(g.a2LN),
        rs.stringOpt(g.a2S),
        rs.intOpt(g.a2ZIP)
      )

    def enc(s:State):DBState = ???
    def dec(dbState:DBState):State = ??? 
  }


  def processMessage(
    historyStore:HistoryStore, 
    sink:Response => Unit
  )(
    msg:Message
  ):Unit = 
    sink(
      historyStore.withCurrentStateDo[Response](msg.parcelId)(
        msg match {
          case NewParcel(key, pid, ts, sender, recipient) => {
            historyStore.appendState(pid, New(pid, ts, sender, recipient)) 
            MessageSuccess(key)
          }
          case m:Message => MessageFailure(m.msgKey, s"No previous State was found and message was $m")
        }
      )(
        state => (state,msg) match {
          case (s:New, m:DistributionHop) => {
            historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, m.distributionCenter, s.recipient))
            MessageSuccess(m.msgKey)
          }
          case (s:InDelivery, m:DistributionHop) => {
            historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, m.distributionCenter, s.intendedRecipientAddress))
            MessageSuccess(m.msgKey)
          }
          case (s:InDelivery, m:Delivery) => if (m.accepted) {
            historyStore.appendState(s.parcelId, Delivered(s.parcelId, m.ts, s.intendedRecipientAddress))
            MessageSuccess(m.msgKey)
          } else {
            historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, s.distributionCenter, s.intendedRecipientAddress)) 
            MessageSuccess(m.msgKey)
          }
          case (s:InDelivery, m:DestinationChange) => {
            historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, s.distributionCenter,  m.newRecipient))
            MessageSuccess(m.msgKey)
          }
          case (s:State, m:Message) => MessageFailure(m.msgKey, s"Illegal Transition Attempt: $s  ===> $m")
        }
      )
    )


}
