package com.scigility.lab.lab10

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.JavaConverters._

object Lab10PartFour extends Lab10SparkJob {
  val bootstrapServers: String = List("ip-10-0-10-152:6667", "ip-10-0-10-251:6667").mkString(",")

  override def runStreamLogic(): Unit = {
    val conf = new SparkConf().setMaster("yarn-client").setAppName("Lab10")
    val ssc = new StreamingContext(conf, Seconds(1))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Array("lab-10"), kafkaParams)
    )

    val resultStream = countSuccessfulByUrl(mapToPageViews(stream))

    resultStream.foreachRDD(rdd => {
      rdd.foreachPartition(p => {
        val prod = new KafkaProducer[String, Int](kafkaParams.asJava)
        p.foreach(kv => {
          prod.send(new ProducerRecord("lab-10-result", kv._1, kv._2)).get
          prod.flush()
        })
      })
    })


    ssc.start()
    ssc.awaitTermination()
  }
}
