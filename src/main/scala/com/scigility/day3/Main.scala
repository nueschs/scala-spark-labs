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
import org.apache.log4j.LogManager
import Program._
import io.circe.syntax._, io.circe.parser.decode

import scala.util.{ Failure, Success, Try }
import pureconfig.generic.auto._
import com.typesafe.config.ConfigFactory

object Main{

  
  final case class SparkApplicationConfig(interval:Long, inputTopic:String, outputTopic:String, brokerList:List[String], groupId:String, driverClassName:String, jdbcURL:String, uname:String, pw:String)

  //This is where we'll jump to, to actually do our spark job
  def sparkMain(spark:SparkSession, applicationConfig:SparkApplicationConfig):Unit = {

    @transient lazy val log = LogManager.getLogger(Main.getClass)
   
    //we set up a map with all our needed kafka parameters that we passed in the config
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> applicationConfig.brokerList.mkString(","),
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> applicationConfig.groupId,
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (true: java.lang.Boolean)
    )

    //create the streaming context based on our spark session and tell it what the streaming batch interval should be
    val ssc = new StreamingContext(spark.sparkContext, Seconds(applicationConfig.interval))

    //set up the actual kafka stream
    val stream = KafkaUtils.createDirectStream[String, String](
        ssc,
        PreferConsistent,
        Subscribe[String, String](Array(applicationConfig.inputTopic), kafkaParams)
      )

    stream.foreachRDD(
      rdd => rdd.foreachPartition(
        partition => Try {
          //again we have non serializable resources that need to be set up per partition so it's all executor local and needs no serialization
          val responseStringify : Response => String = r => r.asJson.noSpaces
          //the kafka sink allows us to write responses to our output topic
          val kafkaSink = MessageSink.kafkaAlgebra(applicationConfig.brokerList)(responseStringify)
          //the jdbc store allows us to retrieve the history, latest state and update the sate of our parcels.
          val jdbcStateStore = HistoryStore.jdbcHistStore(applicationConfig.driverClassName, applicationConfig.jdbcURL, applicationConfig.uname, applicationConfig.pw)

          //I chose to write a per-record program that is independent of spark and just gets called for every record here
          //this is NOT the most efficient way but has several upsides in terms of modularity and maintainability.
          //You may chose to tackle this however you feel most comfortable
          partition
            .toList
            .foreach(
              msg => {
                //we decode the JSON String in the message and if it is succesfull the content of the flatMap will be executed
                decode[Program.Message](msg.value()) match {
                  case Left(err) => throw err
                  case Right(msg) => Program.processMessage(jdbcStateStore, kafkaSink.accept(applicationConfig.outputTopic))(msg)
                }

                /*decode[Program.Message](msg.value()).flatMap(
                  //Here we'll try to execute our program that works on a single record
                  msg => Try {
                    
                  } match {
                    case Success(_) => Right(())
                    case Failure(err) => {
                      Left(err)
                    }
                  }
                ).fold( 
                 err => log.error(err), 
                 _ => ()
                )*/
              }
                  
            )
          //at the end of the partition we clean up our resources
          kafkaSink.close()
          jdbcStateStore.close()
        } match {
          case Success(_) => ()
          case Failure(err) =>  throw err//log.error(err)
        }
        
      )
    )

    ssc.start()
    ssc.awaitTermination()

  }


  //first we jump into the main function and here we have to set up our spark session and read our config. if any of those fail we can immediatly terminate the job
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
