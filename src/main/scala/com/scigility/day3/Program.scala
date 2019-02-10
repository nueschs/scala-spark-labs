package com
package scigility
package day3

import io.circe._, io.circe.generic.semiauto._
import cats._
import scalikejdbc._

object Program {

  //This is a whole bunch of data definitions and declarations
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

  //This is dependent on what library you chose to handle the json messages. I personally chose circe and have to declare the decoders/encoders to be used 
  implicit val encM:Encoder[Message] = deriveEncoder[Message]
  implicit val decR:Decoder[Response] = deriveDecoder[Response]
  implicit val enc:Encoder[Response] = deriveEncoder[Response]
  implicit val dec:Decoder[Message] = deriveDecoder[Message]

  //I also declare how State should be ordered (we need to be able to find the newest)
  implicit val order:Order[State] = Order.from[State]((a,a1) => a.ts.compare(a1.ts))

  //This is how I flatten our different states into the DB (RDBMS don't have a native representation of sum-types)
  final case class DBState(
    demarcator:String, //this field tells us which kind of state it was
    parcelId:String,
    ts:Long,
    aOne:Option[String],
    aTwo:Option[String]
  )

  //All the SQL-ish stuff is from the library I chose to work with
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

  //Here we process a single message. Remember the specification in terms of the state diagram that we had. this is really just a mapping from (current state, incoming message) => (new state, response)
  def processMessage(
    historyStore:HistoryStore, 
    sink:Response => Unit
  )(
    msg:Message
  ):Unit = 
    //we need to write a response anyway so we call the sink (from outside we plug "writing to kafka" into that)
    sink(
      //now we need to find out what kind of response we need to generate. withCurrentStateDo needs a key with which to identify the parcel in the store as well as 2 functions. 1 that tells it what to do if there is NO current state and one if there is
      historyStore.withCurrentStateDo[Response](msg.parcelId)(
        msg match {
          //there's only one valid case if there's no current state. the message has to be a New message. if so we generate the appropriate new state and result in a success response
          case NewParcel(key, pid, ts, sender, recipient) => {
            historyStore.appendState(pid, New(pid, ts, sender, recipient)) 
            MessageSuccess(key)
          }
          //In ALL other cases we fail because the message does not represent a valid state transformation
          case m:Message => MessageFailure(m.msgKey, s"No previous State was found and message was $m")
        }
      )(
        //if we do have a current state we need to match on both the current state and the incoming message. You'll see all of the arrows from the state diagram represented here
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
