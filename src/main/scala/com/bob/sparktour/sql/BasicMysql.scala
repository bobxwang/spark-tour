package com.bob.sparktour

import org.apache.spark.sql.SparkSession

object BasicMysql {

  def main(args: Array[String]) {

    val url = "jdbc:mysql://127.0.0.1:3306/dbname"
    val prop = new java.util.Properties
    prop.setProperty("user", "username")
    prop.setProperty("password", "password")

    val spark = SparkSession.builder()
      .appName("SparksqlTutorial")
      .master("local[*]")
      .config("spark.sql.shuffle.partitions", "6")
      .getOrCreate()
    spark.conf.set("spark.executor.memory", "2g")
    val sqlContext = spark.sqlContext

    val areas = sqlContext.read.jdbc(url, "T_Area_Full", Array("CityCode in ('1101','1201')"), prop)
    val gp = areas.groupBy("CityCode")
    val sorted = gp.count.orderBy("count")
    sorted.show(10)
    sorted.rdd.saveAsTextFile("file:/Users/bob/Desktop/submit")
    spark.sparkContext.stop
  }
}