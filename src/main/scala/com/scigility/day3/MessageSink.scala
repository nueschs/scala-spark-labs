package com
package scigility
package day3

import cats.implicits._
import cats.effect.{Effect, Resource}

import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}

trait MessageSink[F[_], V]{
  def accept(topic:String)(v:V):F[Unit]
}

object MessageSink{
  def mkKafkaConsumerResource[F[_]: Effect](kafkaParamMap:Map[String,Object]):Resource[F, KafkaProducer[String, String]] = Resource.make(
    Effect[F].delay {
      new KafkaProducer[String, String](kafkaParamMap.asJava)
    }
  )(
    producer => Effect[F].delay{ producer.close() }
  )

  def kafkaAlgebra[F[_]: Effect, V](kafkaParamMap:Map[String,Object])(stringify:V => String): Resource[F, MessageSink[F, V]] = mkKafkaConsumerResource(kafkaParamMap).map(
    (producer:KafkaProducer[String, String]) => {
      new MessageSink[F, V] {
        override def accept(topic: String)(v:V): F[Unit] = {
          Effect[F].async(
            callback => {
              producer.send(
                new ProducerRecord(topic, stringify(v)),
                new Callback {
                  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = if (exception == null) callback(Right(metadata)) else callback(Left(exception))
                }
              )
              ()
            }
          ).void
        }
      }
    }
  )
}
