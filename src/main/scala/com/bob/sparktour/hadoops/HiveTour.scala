package com.bob.sparktour.hadoops

import org.apache.spark.sql.SparkSession

object HiveTour {

  def main(args: Array[String]) {

    val spark = SparkSession.builder()
      .appName("basicEstimator")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "6")
      .enableHiveSupport()
      .getOrCreate()
    spark.conf.set("spark.executor.memory", "2g")

    spark.sql("CREATE TABLE IF NOT EXISTS sptest (key INT, value STRING)")
  }
}