package com
package scigility
package day3

import cats._
import cats.data.NonEmptyList
import cats.effect.{ IO, Resource }
import cats.implicits._
import cats.data.State

abstract class HistoryStore[F[_]: Monad, K, V: Order] {

  def getHistory(k:K):F[List[V]]
  def getMostRecent(k:K):F[Option[V]] = getHistory(k).map(_.sorted(Order[V].toOrdering).headOption)
  def appendState(k:K, v:V):F[Unit]
  def withCurrentStateDo[X](k:K)(ifEmpty: => F[X])(vf: V => F[X]):F[X] = getMostRecent(k).flatMap(_.fold(ifEmpty)(vf))
  def update(k:K)(ifEmpty: => F[V])(vf: V => F[V]):F[Unit] = withCurrentStateDo(k)(ifEmpty)(vf).flatMap(v => appendState(k,v))

}

object HistoryStore {
  type Store[K,V] = Map[K,NonEmptyList[V]]

  def kvMemStore[K,V: Order]:HistoryStore[State[Store[K,V], ?], K, V] = new HistoryStore[State[Store[K,V], ?], K, V] {
    def getHistory(k:K):State[Store[K,V], List[V]] = State { s => (s, s.get(k).fold(List.empty[V])(_.toList)) }
    def appendState(k:K, v:V):State[Store[K,V], Unit] = State { s => ((s + (k -> s.get(k).fold(NonEmptyList.one(v))(hist => v :: hist))), ()) }
  }

  def jdbcMemStore[K,V: Order]:Resource[IO, HistoryStore[IO, K,V]] = ???
}
