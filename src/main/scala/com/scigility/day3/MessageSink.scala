package com
package scigility
package day3

import scala.collection.JavaConverters._

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

trait MessageSink[V]{
  def accept(topic:String)(v:V):Unit
  def close():Unit
}

object MessageSink{
  def kafkaAlgebra[V](brokerList:List[String])(stringify:V => String):MessageSink[V] = new MessageSink[V] {
    val producer =  new KafkaProducer[String, String](Map[String,Object](
          "key.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
          "value.serializer" -> "org.apache.kafka.common.serialization.StringSerializer",
          "bootstrap.servers" -> brokerList.mkString(",")
        ).asJava)

    override def accept(topic: String)(v:V): Unit = {
      producer.send(
        new ProducerRecord(topic, stringify(v))
      ).get
      ()
    }

    def close():Unit = producer.close()
  }
  
}
