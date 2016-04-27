package com.bob.sparktour

import org.apache.spark.{SparkConf, SparkContext}

object BasicTransformation {

  def main(args: Array[String]) {

    val sparkConf: SparkConf = new SparkConf()
    if (args.size == 2) {
      sparkConf.setMaster(args(0))
      sparkConf.setAppName(args(1))
    }
    val sc = new SparkContext(sparkConf)
    sc.getConf.toDebugString
    sc.makeRDD(0 to 1000)

    /**
     * if masterURL is local, uses 1 thread only
     * is local[n],uses n threads
     * is local[*],uses as many threads as the number of processors available to jvm, using Runtime.getRuntime.availableProcessors
     */

    //    System.setProperty("spark.executor.memory", "128M")

    val x = sc.parallelize(Array("Joseph", "Jimmy", "Tina", "Thomas", "James", "Cory", "Christine", "Jackeline", "Juan"), 3)
    x.collect.foreach(println)

    val data = sc.parallelize(
      List(
        ("gogu", 1),
        ("gogu", 1),
        ("gogu", 2),
        ("gogu", 3),
        ("dorel", 10),
        ("dorel", 20),
        ("dorel", 10),
        ("gigel", 100),
        ("gigel", 100)
      )
    )
    import scala.collection.mutable.{HashSet => MutableHashSet}
    val results = data
      .aggregateByKey(MutableHashSet.empty[Int])(
        (accum, newVal) => {
          accum += newVal
        }, //in each partition, add the new elements to the hashset
        (partX, partY) => {
          partX ++= partY
        } //combine the hashsets
      )
    results.foreach(tpl => println(tpl._1 + " : " + tpl._2.size))

  }
}