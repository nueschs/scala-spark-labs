package com
package scigility
package day3

import cats._
import cats.implicits._


object Program {
  final case class ApplicationConfig()

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
  final case class MessageSuccess(msgKey:String) extends Response
  final case class MessageFailure(msgKey:String, reason:String) extends Response

  sealed trait State
  final case class New(parcelId:String, ts:Long, sender:Address, recipient:Address) extends State
  final case class InDelivery(parcelId:String, ts:Long, distributionCenter:Address, intendedRecipientAddress:Address) extends State
  final case class Delivered(parcelId:String, ts:Long, recipient:Address) extends State

  def processMessage[F[_]](
    historyStore:HistoryStore[F, String, State], 
    sink:Response => F[Unit]
  )(
    msg:Message
  )(
    implicit F:Monad[F]
  ):F[Unit] = historyStore.withCurrentStateDo[Response](msg.parcelId)(
    msg match {
      case NewParcel(key, pid, ts, sender, recipient) => historyStore.appendState(pid, New(pid, ts, sender, recipient)) *> F.pure(MessageSuccess(key))
      case m:Message => F.pure(MessageFailure(m.msgKey, s"No previous State was found and message was $m"))
    }
  )(
    state => (state,msg) match {
      case (s:New, m:DistributionHop) => historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, m.distributionCenter, s.recipient)) *> F.pure(MessageSuccess(m.msgKey))
      case (s:InDelivery, m:DistributionHop) => historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, m.distributionCenter, s.intendedRecipientAddress)) *> F.pure(MessageSuccess(m.msgKey))
      case (s:InDelivery, m:Delivery) => if (m.accepted)
           historyStore.appendState(s.parcelId, Delivered(s.parcelId, m.ts, s.intendedRecipientAddress)) *> F.pure(MessageSuccess(m.msgKey))
        else
           historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, s.distributionCenter, s.intendedRecipientAddress)) *> F.pure(MessageSuccess(m.msgKey))
      case (s:InDelivery, m:DestinationChange) => historyStore.appendState(s.parcelId, InDelivery(s.parcelId, m.ts, s.distributionCenter,  m.newRecipient)) *> F.pure(MessageSuccess(m.msgKey))
      case (s:State, m:Message) => F.pure(MessageFailure(m.msgKey, s"Illegal Transition Attempt: $s  ===> $m"))
    }
  ).flatMap(sink)


}
