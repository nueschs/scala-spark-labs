package com.scigility.lab.lab10

import com.scigility.lab.lab10.pageview.PageViewGenerator
import org.apache.spark._
import org.apache.spark.streaming._
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent

object Lab10PartOne extends Lab10SparkJob {

  val bootstrapServers: String = List("ip-10-0-10-152:6667", "ip-10-0-10-251:6667").mkString(",")

  def runStreamLogic() = {
    val conf = new SparkConf().setMaster("yarn-client").setAppName("Lab10")
    val ssc = new StreamingContext(conf, Seconds(1))

    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](Array("lab-10"), kafkaParams)
    )

    stream.map(record => (record.key(), record.value())).print()

    ssc.start()
    ssc.awaitTermination()

    PageViewGenerator.stop()
  }

}
