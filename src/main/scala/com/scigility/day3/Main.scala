package com
package scigility
package day3

import cats.implicits._
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.kafka.common.serialization.StringDeserializer
import Program._
import io.circe.syntax._, io.circe.parser.decode

import scala.util.{ Failure, Success, Try }
import pureconfig.generic.auto._
import com.typesafe.config.ConfigFactory

object Main{

  
  final case class SparkApplicationConfig(interval:Long, inputTopic:String, outputTopic:String, brokerList:List[String], groupId:String)


  def sparkMain(spark:SparkSession, applicationConfig:SparkApplicationConfig):Unit = {

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

          partition
            .toList
            .foreach(
              msg => {
                decode[Program.Message](msg.value()).left.map(_.toString).flatMap(
                  msg => Try {
                    Program.processMessage(jdbcStateStore, kafkaSink.accept(applicationConfig.outputTopic)) _
                  } match {
                    case Success(_) => Right(())
                    case Failure(err) => {
                      Left(err.getMessage)
                    }
                  }
                ).fold( 
                  _ => (), 
                  err => {
                    //Log Here
                  }
                )
              }
                  
            )

          
        }
      )
    )

  }



  def main(args:Array[String]):Unit = {
    if (args.length != 1 )
      throw new RuntimeException("Must have exactly one ARG String")
    else{
      val spark = SparkSession.builder().getOrCreate
      pureconfig.loadConfig[SparkApplicationConfig](ConfigFactory.parseString(args(0))).fold(
          failures =>  throw new RuntimeException(failures.toString),
          appConfig => sparkMain(spark, appConfig)
        )
      
    }
      
  }

   
}
