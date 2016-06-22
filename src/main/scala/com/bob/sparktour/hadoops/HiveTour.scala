package com.bob.sparktour.hadoops

import org.apache.spark.sql.hive.HiveContext
import org.apache.spark.{SparkContext, SparkConf}

object HiveTour {

  def main(args: Array[String]) {

    val sparkConf: SparkConf = new SparkConf()
    if (args.size == 2) {
      sparkConf.setMaster(args(0))
      sparkConf.setAppName(args(1))
    }
    val sc = new SparkContext(sparkConf)
    val hiveContext = new HiveContext(sc)
    hiveContext.sql("CREATE TABLE IF NOT EXISTS sptest (key INT, value STRING)")

    sc.stop
  }
}