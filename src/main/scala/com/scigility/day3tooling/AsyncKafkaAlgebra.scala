package com
package scigility
package day3tooling

import cats._
import cats.implicits._
import cats.effect.{ Effect, Resource }
import io.circe.{ Decoder, Encoder }
import io.circe.parser.decode
import org.apache.kafka.clients.consumer.KafkaConsumer
import scala.collection.JavaConverters._
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}


//SUPER DUPER WARNING NOT MADE FOR PARLLELISM AT ALL ALSO SUPER HACKY IN GENERAL
trait AsyncKafkaAlgebra[F[_]] {
  def sendAndAwaitResponse[K:Eq, M:Encoder,R:Decoder](mKeyBy: M => K, rKeyBy:R => K)(message:M):F[R]
}

object AsyncKafkaAlgebra{
  def mkKafkaProducerResource[F[_]: Effect](brokerList:List[String]):Resource[F, KafkaProducer[String, String]] = Resource.make(
    Effect[F].delay {
      new KafkaProducer[String, String](
        Map[String,Object](
          "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
          "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
          "bootstrap.servers" -> brokerList.mkString(",")
        ).asJava
      )
    }
  )(
    producer => Effect[F].delay{ producer.close() }
  )

  def mkKafkaConsumerrResource[F[_]: Effect](brokerList:List[String], groupId:String, topic:String):Resource[F, KafkaConsumer[String, String]] = Resource.make(
    Effect[F].delay {
      val consumer = new KafkaConsumer[String,String](
        Map[String,Object](
          "key.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
          "value.deserializer" -> "org.apache.kafka.common.serialization.StringDeserializer",
          "bootstrap.servers" -> brokerList.mkString(","),
          "group.id" -> groupId
        ).asJava
      )
      consumer.subscribe(List(topic).asJava)
      consumer
    }
  )(
    consumer => Effect[F].delay{ consumer.close() }
  )


  def kafkaAlg[F[_]: Effect](brokerList:List[String], groupId:String, inputTopic:String, outputTopic:String):Resource[F, AsyncKafkaAlgebra[F]] = (
    mkKafkaProducerResource(brokerList),
    mkKafkaConsumerrResource(brokerList, groupId, inputTopic)
  ).mapN(
    (producer, consumer) => new AsyncKafkaAlgebra[F] {

      def send(topic: String)(msg: String): F[RecordMetadata] = {
          Effect[F].async(
            callback => {
              producer.send(
                new ProducerRecord(topic, msg),
                new Callback {
                  override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = if (exception == null) callback(Right(metadata)) else callback(Left(exception))
                }
              )
              ()
            }
          )
      }

      def await[K:Eq, R: Decoder](topic:String, timeout:Long)(k:K, keyBy:R=>K):F[Option[R]] = Effect[F].delay {
        val records = consumer.poll(timeout)
        records
          .asScala
          .map(_.value())
          .map(decode[R])
          .collect{
            case Right(response) => response
          }
          .filter( r => keyBy(r) === k)
          .headOption
      }

      def failOnNone[M, X](message:M)(opt:Option[X]):F[X] = opt.fold(Effect[F].raiseError[X](new RuntimeException(s"no response for $message")))(Effect[F].pure)

      override def sendAndAwaitResponse[K:Eq, M:Encoder,R:Decoder](mKeyBy: M => K, rKeyBy:R => K)(message:M):F[R] = 
        send(inputTopic)(Encoder[M].apply(message).noSpaces) *> 
        await(outputTopic, 30000)(mKeyBy(message), rKeyBy) >>=
        failOnNone(message)

    }
  )

}


