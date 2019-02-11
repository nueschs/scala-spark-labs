package com.scigility.lab.lab10.pageview

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer

import scala.collection.JavaConverters._
import scala.util.Random

/**
  * Quick and dirty adoption of the PageView generator from the Spark examples:
  *
  * https://github.com/apache/spark/blob/master/examples/src/main/scala/org/apache/spark/examples/streaming/clickstream/PageViewGenerator.scala
  */
object PageViewGenerator {
  val pages: Map[String, Double] = Map(
    "http://foo.com/" -> .7,
    "http://foo.com/news" -> 0.2,
    "http://foo.com/contact" -> .1
  )

  val httpStatus: Map[Int, Double] = Map(200 -> .95, 404 -> .05)
  val userZipCode: Map[Int, Double] = Map(94709 -> .5, 94117 -> .5)
  val userID: Map[Int, Double] = Map((1 to 100).map(_ -> .01): _*)

  def pickFromDistribution[T](inputMap: Map[T, Double]): T = {
    val rand = new Random().nextDouble()
    var total = 0.0
    for ((item, prob) <- inputMap) {
      total = total + prob
      if (total > rand) {
        return item
      }
    }
    inputMap.take(1).head._1 // Shouldn't get here if probabilities add up to 1.0
  }

  def getNextClickEvent(): PageView = {
    val id = pickFromDistribution(userID)
    val page = pickFromDistribution(pages)
    val status = pickFromDistribution(httpStatus)
    val zipCode = pickFromDistribution(userZipCode)
    new PageView(page, status, zipCode, id)
  }

  var running = false
  def stop() = running = false

  def start(
             topic: String,
             viewsPerSecond: Float,
             sleepDelayMs: Int,
             bootstrapServers: String
           ): Unit ={

    val kafkaParams = Map[String, Object](
      "key.serializer" -> classOf[StringSerializer],
      "value.serializer" -> classOf[StringSerializer],
      "bootstrap.servers" -> bootstrapServers
    )
    running = true
    new Thread() {
      val prod = new KafkaProducer[String, String](kafkaParams.asJava)

      def delay: Long = {
        def bound(factor: Double) = (sleepDelayMs*factor).intValue()
        val lower = bound(0.8)
        val upper = bound(1.2)
        lower + Random.nextInt(upper - lower).longValue()
      }

      override def run(): Unit = {
        while (running) {
          Thread.sleep(delay)
          val nextEvent = getNextClickEvent()
          prod.send(new ProducerRecord("lab-10", nextEvent.url, nextEvent.toString)).get
          prod.flush()
        }
      }
    }.start()
  }

}
