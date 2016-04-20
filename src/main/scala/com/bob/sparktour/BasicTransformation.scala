package com.bob.sparktour

import org.apache.spark.{SparkConf, SparkContext}

object BasicTransformation {

  def main(args: Array[String]) {
    val sparkConf: SparkConf = new SparkConf().setMaster("local").setAppName("ShiroTutorial")
    val sc = new SparkContext(sparkConf)
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