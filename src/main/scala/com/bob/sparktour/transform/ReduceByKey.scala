package com.bob.sparktour.transform

import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by wangxiang on 18/2/6.
  */
object ReduceByKey extends App {

  val sparkConf: SparkConf = new SparkConf().setMaster("local[*]").setAppName("ReducerByKey")
  val sc = new SparkContext(sparkConf)

  val scoreList = Array(Tuple2("a", 100), Tuple2("b", 100), Tuple2("c", 100), Tuple2("a", 120),
    Tuple2("a", 100))
  val scores = sc.parallelize(scoreList)
  scores.reduceByKey(_ + _).foreach(println(_))

  // it will println as belows
  // (a,320)
  // (c,100)
  // (b,100)
}