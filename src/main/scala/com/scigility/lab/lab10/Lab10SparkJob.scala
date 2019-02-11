package com.scigility.lab.lab10

import com.scigility.lab.lab10.pageview.{PageView, PageViewGenerator}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{IntegerSerializer, StringDeserializer, StringSerializer}
import org.apache.spark.streaming.{Minutes, Seconds}
import org.apache.spark.streaming.dstream.DStream

trait Lab10SparkJob {

  val bootstrapServers: String
  def runStreamLogic(): Unit

  def main(args: Array[String]): Unit = {
    PageViewGenerator.start("lab10", 10, 100, bootstrapServers)

    runStreamLogic()

    PageViewGenerator.stop()
  }
}
