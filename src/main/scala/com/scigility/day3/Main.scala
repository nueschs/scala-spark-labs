package com
package scigility
package day3

import cats.Applicative
import cats.effect.{ ExitCode, IOApp }
import cats.effect.IO
import cats.implicits._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SparkSession.Builder
import Program.ApplicationConfig
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.{CanCommitOffsets, HasOffsetRanges, KafkaUtils}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.kafka.common.serialization.StringDeserializer
import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import Program.Response
import Program.State

object Main extends IOApp {

  
  final case class SparkApplicationConfig(interval:Long, inputTopic:String, outputTopic:String, brokerList:List[String], groupId:String)


  def sparkMain(spark:SparkSession, applicationConfig:SparkApplicationConfig):IO[Unit] = IO {

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> applicationConfig.brokerList.mkString(","),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> applicationConfig.groupId,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    val ssc = new StreamingContext(spark.sparkContext, Seconds(applicationConfig.interval))

    val stream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Subscribe[String, String](Array(applicationConfig.inputTopic), kafkaParams)
      )

    stream.foreachRDD(
      rdd => rdd.foreachPartition(
        partition => {
          val responseStringify : Response => String = r => r.asJson.noSpaces
          val kafkaSink = MessageSink.kafkaAlgebra(kafkaParams)(responseStringify)
          val jdbcStateStore = HistoryStore.jdbcMemStore[String, State]

          (kafkaSink, jdbcStateStore).tupled.use(
            partition.toList.traverse(Program.processMessage(algs._1, algs._2.accept(applicationConfig.outputTopic)))
          )
            .void
            .unsafeRunSync
          
        }
      )
    )

  }



  override def run(args: List[String]): IO[ExitCode] = 
    args.headOption.fold(
      IO.raiseError[String](new RuntimeException("Must have a String Config as Parameter"))
    )(
      IO.pure _ 
    ).flatMap(
      confString => 
      (
        IO { SparkSession.builder().getOrCreate },
        ConfigAlgebra[IO].readConfigFromString[SparkApplicationConfig](confString)
      ).mapN(sparkMain _)
    )

  
}
