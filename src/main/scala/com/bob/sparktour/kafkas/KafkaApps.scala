package com.bob.sparktour.kafkas

import java.util.concurrent.{Executors, ExecutorService}
import java.util.{Date, Properties}

import kafka.consumer.{KafkaStream, Consumer, ConsumerConfig}
import kafka.producer.{Producer, KeyedMessage, ProducerConfig}

import scala.util.Random

object KafkaApps extends App {

  val zk = "192.168.2.200:2182"
  val kafkabroke = "192.168.2.200:9092"
  val topic = "bbtestbb"

  def run(func: => Unit) = {
    new Thread(new Runnable {
      override def run(): Unit = func
    }).start()
  }

//  val kp = new KProducer(10, topic, kafkabroke)
//  run(kp.send())

  val kc = new KConsumer(zk, "gpc", topic, 100)
  run(kc.run(10))

  /**
   *
   * @param events 循环列表值
   * @param topic
   * @param brokers
   */
  class KProducer(val events: Int, topic: String, brokers: String) {
    val rnd = new Random()
    val props = new Properties()
    props.put("metadata.broker.list", brokers)
    props.put("serializer.class", "kafka.serializer.StringEncoder")
    props.put("producer.type", "async")
    val config = new ProducerConfig(props)

    def send() = {
      val t = System.currentTimeMillis()
      val producer = new Producer[String, String](config)
      (1 to events).foreach { nEvents =>
        val runtime = new Date().getTime();
        val ip = "192.168.2." + rnd.nextInt(255);
        val msg = s"${runtime},${nEvents},www.example.com,${ip}"
        val data = new KeyedMessage[String, String](topic, ip, msg)
        producer.send(data);
        println("produce sent per second: " + events * 1000 / (System.currentTimeMillis() - t));
      }
      producer.close();
    }
  }

  class KConsumer(val zk: String, val groupId: String,
                  val topic: String,
                  val delay: Long) {

    val config = createConsumerConfig(zk, groupId)
    val consumer = Consumer.create(config)
    var executor: ExecutorService = null

    def shutdown() = {
      if (consumer != null)
        consumer.shutdown()
      if (executor != null)
        executor.shutdown()
    }

    def createConsumerConfig(zookeeper: String, groupId: String): ConsumerConfig = {
      val props = new Properties()
      props.put("zookeeper.connect", zookeeper)
      props.put("group.id", groupId)
      props.put("auto.offset.reset", "largest")
      props.put("zookeeper.session.timeout.ms", "400")
      props.put("zookeeper.sync.time.ms", "200")
      props.put("auto.commit.interval.ms", "1000")
      new ConsumerConfig(props)
    }

    def run(numThreads: Int) = {
      val topicCountMap = Map(topic -> numThreads)
      val consumerMap = consumer.createMessageStreams(topicCountMap)
      executor = Executors.newFixedThreadPool(numThreads)
      var threadNumber = 0
      consumerMap.get(topic) match {
        case Some(streams) => {
          streams.foreach {
            stream => {
              executor.submit(new ScalaConsumerTest(stream, threadNumber, delay))
              threadNumber += 1
            }
          }
        }
        case None => println("nothing can read")
      }
    }

    def clouse(threadNo: Int) = {
      threadNo
    }
  }

  class ScalaConsumerTest(val stream: KafkaStream[Array[Byte], Array[Byte]], val threadNumber: Int, val delay: Long) extends Runnable {

    def run {
      val it = stream.iterator()
      while (it.hasNext()) {
        val msg = new String(it.next().message(), "utf-8")
        println("consumer " + System.currentTimeMillis() + ",Thread " + threadNumber + ": " + msg)
      }
      println("Shutting down Thread: " + threadNumber);
    }
  }

}