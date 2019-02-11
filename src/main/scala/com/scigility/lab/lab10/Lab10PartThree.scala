package com.scigility.lab.lab10

import com.scigility.lab.lab10.pageview.PageView
import org.apache.spark.SparkConf
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.{Minutes, Seconds, StreamingContext}

object Lab10PartThree extends Lab10SparkJob {
  val bootstrapServers: String = List("ip-10-0-10-152:6667", "ip-10-0-10-251:6667").mkString(",")

  override def runStreamLogic(): Unit = {
    val conf = new SparkConf().setMaster("yarn-client").setAppName("Lab10")
    val ssc = new StreamingContext(conf, Seconds(1))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Array("lab-10"), kafkaParams)
    )

    val countStream: DStream[(String, Int)] = countSuccessfulByUrl(mapToPageViews(stream))
    countStream.print()

    ssc.start()
    ssc.awaitTermination()
  }

}
