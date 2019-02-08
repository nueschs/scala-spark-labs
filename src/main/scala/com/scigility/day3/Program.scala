package com
package scigility
package day3

import io.circe._, io.circe.generic.semiauto._
import cats._
import scalikejdbc._

object Program {


  sealed trait Message{
    def msgKey:String
    def parcelId:String
    def ts:Long
  }
  final case class NewParcel(msgKey:String, parcelId:String, ts:Long, sender:String, recipient:String) extends Message
  final case class DistributionHop(msgKey:String, parcelId:String, ts:Long, distributionCenter:String) extends Message
  final case class Delivery(msgKey:String, parcelId:String, ts:Long, accepted:Boolean) extends Message
  final case class DestinationChange(msgKey:String, parcelId:String, ts:Long, newRecipient: String) extends Message

  sealed trait Response{
    def msgKey:String
  }
  final case class MessageFailure(msgKey:String, reason:String) extends Response
  final case class MessageSuccess(msgKey:String) extends Response
  

  sealed trait State{
    def ts:Long
  }
  final case class New(parcelId:String, ts:Long, sender:String, recipient:String) extends State
  final case class InDelivery(parcelId:String, ts:Long, distributionCenter:String, intendedRecipientAddress:String) extends State
  final case class Delivered(parcelId:String, ts:Long, recipient:String) extends State


  implicit val encM:Encoder[Message] = deriveEncoder[Message]
  implicit val decR:Decoder[Response] = deriveDecoder[Response]
  implicit val enc:Encoder[Response] = deriveEncoder[Response]
  implicit val dec:Decoder[Message] = deriveDecoder[Message]

  implicit val order:Order[State] = Order.from[State]((a,a1) => a.ts.compare(a1.ts))

  final case class DBState(
    demarcator:String,
    parcelId:String,
    ts:Long,
    aOne:Option[String],
    aTwo:Option[String]
  )

  object DBState  extends SQLSyntaxSupport[DBState] {
    override val tableName = "states"


    def apply(g: ResultName[DBState])(rs: WrappedResultSet) =
      new DBState(
        rs.string(g.demarcator), 
        rs.string(g.parcelId),
        rs.long(g.ts),
        rs.stringOpt(g.aOne),
        rs.stringOpt(g.aTwo)
      )

    def enc(s:State):DBState = s match {
      case New(parcelId, ts, sender, recipient) => DBState("new", parcelId, ts, Some(sender), Some(recipient))
      case InDelivery(parcelId, ts, distributionCenter, intendedRecipientAddress) => DBState("indelivery", parcelId, ts, Some(distributionCenter), Some(intendedRecipientAddress))
      case Delivered(parcelId,ts,recipient) => DBState("delivered", parcelId, ts, Some(recipient), None)
    }
    def dec(dbState:DBState):State = dbState match {
      case DBState("new", parcelId, ts, Some(a1), Some(a2)) => New(parcelId, ts, a1, a2)
      case DBState("indelivery", parcelId, ts, Some(a1), Some(a2)) => InDelivery(parcelId,ts,a1,a2)
      case DBState("delivered", parcelId, ts, Some(a1), None) => Delivered(parcelId,ts,a1)
      case errState:DBState => throw new RuntimeException(s"Invalid DBState: $errState") 
    }
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
