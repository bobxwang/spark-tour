package com.bob.sparktour.kafkas

import java.util.concurrent.{Executors, ExecutorService}
import java.util.{Date, Properties}

import kafka.api.{OffsetRequest => aOffsetRequest, FetchRequestBuilder, PartitionOffsetRequestInfo}
import kafka.common.{ErrorMapping, TopicAndPartition}
import kafka.consumer.{KafkaStream, Consumer, ConsumerConfig}
import kafka.javaapi.consumer.SimpleConsumer
import kafka.javaapi._
import kafka.javaapi.message.ByteBufferMessageSet
import kafka.producer.{Producer, KeyedMessage, ProducerConfig}

import scala.util.Random

import scala.collection.JavaConversions._

object KafkaApps extends App {

  val zk = "192.168.2.200:2182"
  val kafkabroke = "192.168.2.200:9092"
  val topic = "bbtestbb-new"

  def run(func: => Unit) = {
    new Thread(new Runnable {
      override def run(): Unit = func
    }).start()
  }

  //  val kp = new KProducer(10, topic, kafkabroke)
  //  run(kp.send())

  //  val kc = new KConsumer(zk, "gpddddcab", topic, 100)
  //  run(kc.run(10))

  val lkc = new LKConsumer(topic, List("192.168.2.200"), 9092, 0)
  lkc.run(2)

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

  class LKConsumer(val topic: String, brokers: List[String], port: Int, partition: Int) {

    var m_replicaBrokers: scala.collection.mutable.ListBuffer[String] = scala.collection.mutable.ListBuffer.empty[String]

    def run(offset: Long) = {
      val metadata = findLeader()
      if (metadata == null) {
        println("can't find metadata for topic and partition, exiting")
      }
      if (metadata.leader == null) {
        println("can't find leader for topic and partition, exiting")
      }
      val leaderBroker = metadata.leader.host
      val clientName = s"Client_${topic}_${partition}"
      val consumer = new SimpleConsumer(leaderBroker, port, 100000, 64 * 1024, clientName)
      val readOffset = findLastOffset(consumer, topic, partition, kafka.api.OffsetRequest.EarliestTime)
      println(s"righnt now offset is ${readOffset}")
      val req: kafka.api.FetchRequest = new FetchRequestBuilder()
        .clientId(consumer.clientId)
        .addFetch(topic, partition, offset, 10000)
        .build()
      val fetchResponse: kafka.javaapi.FetchResponse = consumer.fetch(req)
      if (fetchResponse.hasError) {
        val errorCode = fetchResponse.errorCode(topic, partition)
        if (errorCode == ErrorMapping.OffsetOutOfRangeCode) {
          println(s"get fetch request with offset out of range: [${offset}]")
        }
      } else {
        val msgSet: ByteBufferMessageSet = fetchResponse.messageSet(topic, partition)
        msgSet.foreach(x => {
          println(x.offset)
          println(x.nextOffset)
          val payload = x.message.payload
          val bytes: Array[Byte] = new Array(payload.limit)
          payload.get(bytes)
          println(s"${x.offset} : ${new String(bytes, "UTF-8")}")
        })
      }
    }

    def findLastOffset(simpleConsumer: SimpleConsumer, topic: String, partition: Int, whichTime: Long): Long = {
      val topicAndPartition: TopicAndPartition = new TopicAndPartition(topic, partition)
      val requestInfo: Map[TopicAndPartition, PartitionOffsetRequestInfo] = Map(topicAndPartition -> new PartitionOffsetRequestInfo(whichTime, 1))
      val request: OffsetRequest = new OffsetRequest(requestInfo, aOffsetRequest.CurrentVersion, simpleConsumer.clientId)
      val response: OffsetResponse = simpleConsumer.getOffsetsBefore(request)
      if (response.hasError) {
        println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition))
        0
      } else {
        response.offsets(topic, partition)(0)
      }
    }

    def findLeader(): PartitionMetadata = {
      import scala.util.control._
      var returnMetaData: PartitionMetadata = null
      val loop = new Breaks
      loop.breakable {
        brokers.foreach(x => {
          val consumer = new SimpleConsumer(x, port, 100000, 64 * 1024, "leaderLookup")
          val req = new TopicMetadataRequest(List(topic))
          val resp: TopicMetadataResponse = consumer.send(req)
          val metaData = resp.topicsMetadata
          metaData.foreach(y => {
            y.partitionsMetadata.foreach(z => {
              if (z.partitionId == partition) {
                returnMetaData = z
                if (consumer != null) {
                  consumer.close()
                }
                loop.break
              }
            })
          })
        })
      }
      if (returnMetaData != null) {
        m_replicaBrokers.clear()
        returnMetaData.replicas.foreach(x => {
          m_replicaBrokers.append(x.host)
        })
      }
      returnMetaData
    }
  }

  /**
   * high level consumer
   * @param zk
   * @param groupId
   * @param topic
   * @param delay
   */
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
      props.put("auto.offset.reset", "smallest")
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


  /**
   * topic has many partitions
   *
   * each partitioned message has a unique sequence id called as "offset"
   *
   * replicas r nothing but "backups" of a partition,never read or write data,just used to prevent data loss
   *
   * Brokers are simple system responsible for maintaining the pub-lished data. Each broker may have zero or more partitions per topic. Assume, if there are N partitions in a topic and N number of brokers, each broker will have one partition.
   *
   * kafka cluster having more than one broker
   *
   * producers r the publiser of messages to one or more kafka topic
   *
   * consumers read data from brokers, subscribes to one or more topics and consumer the published message by pulling data from the broker
   *
   * leader is the node responsible for all reads and writes for the given partition, every partition has one server acting as a leader
   *
   * kafka is simply a collection of topics split into one or more partitions,a partition is a linearly ordered sequeence of messages,where
   * each message is identified by their index(
   */

}