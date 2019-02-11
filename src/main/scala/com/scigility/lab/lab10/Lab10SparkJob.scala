package com.scigility.lab.lab10

import com.scigility.lab.lab10.pageview.{PageView, PageViewGenerator}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.{IntegerSerializer, StringDeserializer, StringSerializer}
import org.apache.spark.streaming.{Minutes, Seconds}
import org.apache.spark.streaming.dstream.DStream

trait Lab10SparkJob {

  val bootstrapServers: String
  def runStreamLogic(): Unit

  lazy val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> bootstrapServers,
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "key.serializer" -> classOf[StringSerializer],
    "value.serializer" -> classOf[IntegerSerializer],
    "group.id" -> "lab10",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  def mapToPageViews(in: DStream[ConsumerRecord[String, String]]): DStream[(String, PageView)] = {
    in.map(record => (record.key(), PageView.fromString(record.value())))
  }

  def countSuccessfulByUrl(in: DStream[(String, PageView)]): DStream[(String, Int)] = {
    in.filter(_._2.status == 200)
      .groupByKeyAndWindow(Minutes(5), Seconds(10))
      .mapValues(_.size)
  }

  def main(args: Array[String]): Unit = {
    PageViewGenerator.start("lab10", 10, 100, bootstrapServers)

    runStreamLogic()

    PageViewGenerator.stop()
  }
}
