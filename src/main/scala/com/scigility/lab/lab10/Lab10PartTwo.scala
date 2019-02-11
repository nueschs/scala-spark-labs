package com.scigility.lab.lab10

import com.scigility.lab.lab10.pageview.PageView
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Seconds, StreamingContext}

object Lab10PartTwo extends Lab10SparkJob {
  val bootstrapServers: String = List("ip-10-0-10-152:6667", "ip-10-0-10-251:6667").mkString(",")

  override def runStreamLogic(): Unit = {
    val conf = new SparkConf().setMaster("yarn-client").setAppName("Lab10")
    val ssc = new StreamingContext(conf, Seconds(1))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Array("lab-10"), kafkaParams)
    )

    stream
      .map(record => {
        val pageView =  PageView.fromString(record.value())
        (pageView.userID, pageView)
      })
      .filter(_._2.status == 404)
      .filter(_._2.userID > 50)
      .print()

    ssc.start()
    ssc.awaitTermination()
  }
}
