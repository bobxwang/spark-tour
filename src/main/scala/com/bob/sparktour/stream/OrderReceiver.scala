package com.bob.sparktour.stream

import java.io.{InputStreamReader, BufferedReader}
import java.net.Socket

import org.apache.spark.storage.StorageLevel
import org.apache.spark.streaming.receiver.Receiver

case class Item(id: Int, cost: Int)

case class Order(id: Int, total: Int, items: List[Item] = null)

/**
 * 自定义订单流
 */
class OrderReceiver(host: String, port: Int) extends Receiver[Order](StorageLevel.MEMORY_ONLY) {
  override def onStart(): Unit = {
    println("staring...")

    val thread = new Thread("Receiver") {
      override def run(): Unit = receive
    }
    thread.start()
  }

  def receive() = {
    val socket = new Socket(host, port)
    var currentOrder: Order = null
    var currentItems: List[Item] = null

    val reader = new BufferedReader(new InputStreamReader(socket.getInputStream, "UTF-8"))
    while (!isStopped()) {
      val userInput = reader.readLine()
      if (userInput == null) {
        stop("Stream has ended")
      }
      else {
        val parts = userInput.split(" ")
        if (parts.length == 2) {
          if (currentOrder != null) {
            store(Order(currentOrder.id, currentOrder.total, currentItems))
          }

          currentOrder = Order(parts(0).toInt, parts(1).toInt)
          currentItems = List[Item]()
        } else {
          currentItems = Item(parts(0).toInt, parts(1).toInt) :: currentItems
        }
      }
    }
  }

  override def onStop(): Unit = stop("I am done")
}