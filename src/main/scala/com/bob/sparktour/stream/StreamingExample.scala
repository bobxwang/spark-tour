package com.bob.sparktour

import com.bob.sparktour.stream.{OrderReceiver, Order}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.{SparkContext, SparkConf, Logging}

/** Utility functions for Spark Streaming examples. */
object StreamingExample extends Logging {

  /** Set reasonable logging levels for streaming if the user has not configured log4j. */
  def setStreamingLogLevels() {
    val log4jInitialized = Logger.getRootLogger.getAllAppenders.hasMoreElements
    if (!log4jInitialized) {
      // We first log something to initialize Spark's default logging, then we override the
      // logging level.
      logInfo("Setting log level to [WARN] for streaming example." +
        " To override add a custom log4j.properties to the classpath.")
      Logger.getRootLogger.setLevel(Level.WARN)
    }
  }

  def main(args: Array[String]) {
    val config = new SparkConf().setAppName("OrderStream")
    val sc = new SparkContext(config)
    val ssc = new StreamingContext(sc, Seconds(5))
    val stream: DStream[Order] = ssc.receiverStream(new OrderReceiver("127.0.0.1", 8000))
    stream.foreachRDD(x => {
      x.foreach(order => {
        println(order.id)
        order.items.foreach(println)
      })
    })
    ssc.start()
    ssc.awaitTermination()
  }
}